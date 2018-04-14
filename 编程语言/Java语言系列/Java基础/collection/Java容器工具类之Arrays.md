# Java容器工具类之Arrays
Java Collections Framework的一个工具类，我们可以类比`Collections`工具类。使用这个工具类的方式: `import java.util.Arrays`。`Arrays`工具类提供了一下几类方法, 需要重点说明的是，几乎所有的方法都对几乎所有数据类型进行了方法重载：

1. 二分查找，查找指定元素的索引值——`binarySearch()`

标准Java SE API对方法返回值的描述：

`index of the search key, if it is contained in the array within the specified range; otherwise, (-(insertion point) - 1). The insertion point is defined as the point at which the key would be inserted into the array: the index of the first element in the range greater than the key, or toIndex if all elements in the range are less than the specified key. Note that this guarantees that the return value will be >= 0 if and only if the key is found. `

示例代码：
```java
public class TestArrays {
    public static void main(String[] args){
        
        int[] array = {10, 20, 30, 40, 50, 60, 70, 80};
        
        System.out.println(testBinarySearch(array, 10));
        System.out.println(testBinarySearch(array, 45));
    }
    public static int testBinarySearch(int[] array, int key) {
        return Arrays.binarySearch(array, key);
    }
}

```
程序结果：
```java
0
-5
```

2. 截取数组：`copeOf`和`copeOfRange`方法
我们看一下这两个方法的具体方法签名：

```java
public static <T> T[] copyOfRange(T[] original, int from, int to);

public static <T> T[] copyOf(T[] original, int newLength);

public static <T, U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType)
```

示例代码：

```java
public static void testCopyOf() {
    int []arr = {10,20,30,40,50};
    int []arr1 = Arrays.copyOf(arr, 3);
    output(arr1);
}
```

```java
public static void testCopyOfRange() {
   int []arr = {10,20,30,40,50};
   int []arr1 = Arrays.copyOfRange(arr,1,3);
   output(arr1);
}

```


3. 比较元素是否相等 `equals`以及`hashCode()`方法
提供了各种数据类型数组的比较和求数组哈希值的方法，如下示例代码：

```java
int []arr1 = {1,2,3};
int []arr2 = {1,2,3};
System.out.println(Arrays.equals(arr1,arr2));
System.out.println(Arrays.hashCode(arr1));
```

4. 填充数组 `fill`方法
重载了各种数据类型的数组填充方法。

```java
int []arr = new int[5];
Arrays.fill(arr, 2);
```

5. 数组排序算法 `sort`
重载了各种数据类型的排序算法。

```java
int []arr = {3,2,1,5,4};
Arrays.sort(arr,1,3);
```