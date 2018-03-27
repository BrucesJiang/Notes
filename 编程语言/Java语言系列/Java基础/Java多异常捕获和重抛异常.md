# Java多异常捕获和重抛异常

## 多异常捕获
在Java 7 中， `catch`块已经被改进，可以在单个`catch`块中处理多个异常。如果我们在代码中捕获多个异常并且它们具有相似的处理代码，那么使用这个增强功能可以减少代码冗余（Code Duplication）。我们通过一个例子来理解这个增强功能。

在Java 7之前，我们用来逐个捕获多个异常，如下所示。
```java
catch (IOException ex) {
     logger.error(ex);
     throw new MyException(ex.getMessage());
}catch (SQLException ex) {
     logger.error(ex);
     throw new MyException(ex.getMessage());
}

```
在Java 7 之后，我们可以在一个`catch`块中捕获所有异常：
```java
catch(IOException | SQLException ex){
     logger.error(ex);
     throw new MyException(ex.getMessage());
}

```
如果catch块处理多个异常，可以使用`管道（|）`将它们分开，在这种情况下，异常参数（ex）是`final`的，所以我们不能更改它。 由该特征生成的字节码较小并且减少了码冗余。

## 重抛异常（Rethrown Exception）
Java 7 另外一项重要的改进是在编译器层面对重抛异常机制（Compiler analysis of rethrown exceptions）做了改进。Java rethrow异常允许我们在方法声明的throws子句中指定更多特定的异常类型。

```java
public class Java7MultipleExceptions {

    public static void main(String[] args) {
        try{
            rethrow("abc");
        }catch(FirstException | SecondException | ThirdException e){
            //below assignment will throw compile time exception since e is final
            //e = new Exception();
            System.out.println(e.getMessage());
        }
    }

    static void rethrow(String s) throws FirstException, SecondException,
            ThirdException {
        try {
            if (s.equals("First"))
                throw new FirstException("First");
            else if (s.equals("Second"))
                throw new SecondException("Second");
            else
                throw new ThirdException("Third");
        } catch (Exception e) {
            //below assignment disables the improved rethrow exception type checking feature of Java 7
            // e=new ThirdException();
            throw e;
        }
    }

    static class FirstException extends Exception {

        public FirstException(String msg) {
            super(msg);
        }
    }

    static class SecondException extends Exception {

        public SecondException(String msg) {
            super(msg);
        }
    }

    static class ThirdException extends Exception {

        public ThirdException(String msg) {
            super(msg);
        }
    }

}

```
在重新抛出方法中，catch块正在捕获异常，但它不是throws子句的一部分。Java 7编译器分析完整的try块以检查抛出什么类型的异常，然后从catch块中重新抛出。

**请注意，如果更改catch块参数，则此分析将被禁用。** 这句话目前不理解。