# HashMap的常用方法

首先，在实现元素计数器时，HashMap十分有用，例如：

```java
HashMap<String, Integer> countMap = new HashMap<String, Integer>();
//  a lot of a's like the following
if(countMap.keySet().contains(a)) {
    countMap.put(a, countMap.get(a)+1);
}else {
    countMap.put(a, 1);
}
```

另外，HashMap常用方法实例：

1. Loop Through HashMap
```java
Intertor iter = mp.entrySet().iterator();
while(it.hashNext()) {
    Map.Entry pairs = (Map.Entry)it.next();
    System.out.println(pairs.getKey() + ", Value = " + entry.getValue());
}

Map<Integer, Integer> map = new Hash<Integer, Integer>();
for(map.Entry<Integer, Integer> entry : map.entrySet()) {
    System.out.println(entry.getKey() + ", Value = " + entry.getValue());
}
```
2. Print HashMap
```java
public static void printMap(Map mp) {
    Iterator it = map.entrySet().iterator();
    while(it.hasNext()) {
        Map.Entry pairs = (Map.Entry)it.next();
        System.out.println(pairs.getKey() + " = " + pairs.getValue());
        it.remove(); /avoids a ConcurrentModificationException
    }
}
```
3. Sort HashMap by Value
```java
class ValueComparator implements Comparator<String> {
    Map<String, Integer> base;
    public ValueComparator(Map<String, Integger> base) {
        this.base = base;
    }

    public int compare(String a, String b) {
        if(base.get(a) >= base.get(b)) {
            return -1;
        }else {
            return 1;
        }//returns 0 would mrege keys
    }
}

HashMap<String, Integer> countMap = new HashMap<String, Integer>();
//add a lot of entries
countMap.put("a", 10);
countMap.put("b", 20);
 
ValueComparator vc =  new ValueComparator(countMap);
TreeMap<String,Integer> sortedMap = new TreeMap<String,Integer>(vc);
 
sortedMap.putAll(countMap);  
 
printMap(sortedMap);

```
