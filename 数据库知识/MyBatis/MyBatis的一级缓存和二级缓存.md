# MyBatis的一级缓存和二级缓存

MyBatis自带的缓存有一级缓存和二级缓存，本文的主要内容就是MyBatis的自带缓存以及其使用原则。


## 一级缓存

MyBatis的一级缓存是指Session缓存。一级缓存的作用于默认是SqlSession。MyBatis默认开启一级缓存。也就是在同一个SqlSession中，执行相同的SQL查询，第一次会去数据库查询，并写到缓存中；第二次以后则直接去缓存中取。当执行SQL查询期间发生了增删改的操作，MyBatis会将SqlSession的缓存清空。

一级缓存的范围有`SESSION`和`STATEMENT`两种，默认为`SESSSION`，如果希望禁用一级缓存，可以将一级缓存的范围指定为`STATEMENT`，这样每次执行完一条`Mapper`中的语句后都会将一级缓存清除。

如果需要更改一级缓存的范围，可以在MyBatis的配置文件中，通过`localCacheScope`指定。

```xml
<setting name="localCacheScope" value="STATEMENT" />
```

**注意：**

当MyBatis整合Spring后，直接通过Spring注入Mapper。如果不是在同一个事务中，每个Mapper的每次查询操作都对应一个全新的`SqlSession`实例，这时就不会有一级缓存的命中，但是在同一个事务中时共用的是同一个`SqlSession`。


## 二级缓存


Mybatis的二级缓存是指mapper映射文件。二级缓存的作用域是同一个namespace下的mapper映射文件内容，多个SqlSession共享。Mybatis需要手动设置启动二级缓存。

二级缓存是默认启用的(要生效需要对每个Mapper进行配置)，如想取消，则可以通过Mybatis配置文件中的元素下的子元素来指定cacheEnabled为false。

```xml
<settings>
  <setting name="cacheEnabled" value="false" />
</settings>
```

cacheEnabled默认是启用的，只有在该值为true的时候，底层使用的Executor才是支持二级缓存的CachingExecutor。具体可参考Mybatis的核心配置类org.apache.ibatis.session.Configuration的newExecutor方法实现。 其实现源码：

```java
public Executor newExecutor(Transaction transaction) {        
	return this.newExecutor(transaction, this.defaultExecutorType);
}    

public Executor newExecutor(Transaction transaction, ExecutorType executorType) {
    executorType = executorType == null ? this.defaultExecutorType : executorType;
    executorType = executorType == null ? ExecutorType.SIMPLE : executorType;
    Object executor;        
    if (ExecutorType.BATCH == executorType) {
        executor = new BatchExecutor(this, transaction);
    } else if (ExecutorType.REUSE == executorType) {
        executor = new ReuseExecutor(this, transaction);
    } else {
        executor = new SimpleExecutor(this, transaction);
    }        

    if (this.cacheEnabled) {//设置为true才执行的
        executor = new CachingExecutor((Executor)executor);
    }

    Executor executor = (Executor)this.interceptorChain.pluginAll(executor);        
    return executor;
}

```

要使用二级缓存除了上面一个配置外，我们还需要在我们每个DAO对应的Mapper.xml文件中定义需要使用的cache

```xml
<mapper namespace="...UserMapper">
    <cache/><!-- 加上该句即可，使用默认配置、还有另外一种方式，在后面写出 -->
    ...
</mapper>
```

具体可以看org.apache.ibatis.executor.CachingExecutor类的以下实现其中使用的cache就是我们在对应的Mapper.xml中定义的cache。

```java
public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
    BoundSql boundSql = ms.getBoundSql(parameterObject);
    CacheKey key = this.createCacheKey(ms, parameterObject, rowBounds, boundSql);        return this.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
}    

public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
    Cache cache = ms.getCache();        
    if (cache != null) {//第一个条件 定义需要使用的cache  
        this.flushCacheIfRequired(ms);            
    if (ms.isUseCache() && resultHandler == null) {//第二个条件 需要当前的查询语句是配置了使用cache的，即下面源码的useCache()是返回true的  默认是true
            this.ensureNoOutParams(ms, parameterObject, boundSql);
            List<E> list = (List)this.tcm.getObject(cache, key);                
            if (list == null) {
                list = this.delegate.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);                    
                this.tcm.putObject(cache, key, list);
            }                
            return list;
        }
    }        
    return this.delegate.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
}

```
还有一个条件就是需要当前的查询语句是配置了使用cache的，即上面源码的useCache()是返回true的，默认情况下所有select语句的useCache都是true，如果我们在启用了二级缓存后，有某个查询语句是我们不想缓存的，则可以通过指定其useCache为false来达到对应的效果

如果我们不想该语句缓存，可使用`useCache="false"`

```xml
<select id="selectByPrimaryKey" resultMap="BaseResultMap" parameterType="java.lang.String" useCache="false">
    select
    <include refid="Base_Column_List"/>
    from tuser
    where id = #{id,jdbcType=VARCHAR}
</select>
```

### cache定义的两种使用方式
上面说了要想使用二级缓存，需要在每个DAO对应的Mapper.xml文件中定义其中的查询语句需要使用cache来缓存数据的。 这有两种方式可以定义，一种是通过cache元素定义，一种是通过cache-ref元素来定义。 需要注意的是对于同一个Mapper来讲，只能使用一个Cache，当同时使用了和时，定义的优先级更高(后面的代码会给出原因)。 Mapper使用的Cache是与我们的Mapper对应的namespace绑定的，一个namespace最多只会有一个Cache与其绑定

#### cache元素定义
使用cache元素来定义使用的Cache时，最简单的做法是直接在对应的Mapper.xml文件中指定一个空的元素(看前面的代码)，这个时候Mybatis会按照默认配置创建一个Cache对象，准备的说是PerpetualCache对象，更准确的说是LruCache对象（底层用了装饰器模式）。 
具体的可看org.apache.ibatis.builder.xml.XMLMapperBuilder中的cacheElement()方法解析cache元素的逻辑。

```java
private void configurationElement(XNode context) {        
	try {
        String namespace = context.getStringAttribute("namespace");            
        if (namespace.equals("")) {                
        	throw new BuilderException("Mapper's namespace cannot be empty");
        } else {                
        	this.builderAssistant.setCurrentNamespace(namespace);                
        	this.cacheRefElement(context.evalNode("cache-ref"));                
        	this.cacheElement(context.evalNode("cache"));//执行在后面
            this.parameterMapElement(context.evalNodes("/mapper/parameterMap"));               
            this.resultMapElements(context.evalNodes("/mapper/resultMap"));                
            this.sqlElement(context.evalNodes("/mapper/sql"));                
            this.buildStatementFromContext(context.evalNodes("select|insert|update|delete"));
        }
    } catch (Exception var3) {            
    	throw new BuilderException("Error parsing Mapper XML. Cause: " + var3, var3);
    }
}

private void cacheRefElement(XNode context) {       
	if (context != null) {            
		this.configuration.addCacheRef(this.builderAssistant.getCurrentNamespace(), context.getStringAttribute("namespace"));
        CacheRefResolver cacheRefResolver = new CacheRefResolver(this.builderAssistant, context.getStringAttribute("namespace"));            
        try {
            cacheRefResolver.resolveCacheRef();
        } catch (IncompleteElementException var4) {                
        	this.configuration.addIncompleteCacheRef(cacheRefResolver);
        }
    }

}    

private void cacheElement(XNode context) throws Exception {        
	if (context != null) {
        String type = context.getStringAttribute("type", "PERPETUAL");
        Class<? extends Cache> typeClass = this.typeAliasRegistry.resolveAlias(type);
        String eviction = context.getStringAttribute("eviction", "LRU");
        Class<? extends Cache> evictionClass = this.typeAliasRegistry.resolveAlias(eviction);
        Long flushInterval = context.getLongAttribute("flushInterval");
        Integer size = context.getIntAttribute("size");            
        boolean readWrite = !context.getBooleanAttribute("readOnly", false).booleanValue();
        Properties props = context.getChildrenAsProperties();            
        this.builderAssistant.useNewCache(typeClass, evictionClass, flushInterval, size, readWrite, props);//如果同时存在<cache>和<cache-ref>，这里的设置会覆盖前面的cache-ref的缓存
    }
}

```

空cache元素定义会生成一个采用最近最少使用算法最多只能存储1024个元素的缓存，而且是可读写的缓存，即该缓存是全局共享的，任何一个线程在拿到缓存结果后对数据的修改都将影响其它线程获取的缓存结果，因为它们是共享的，同一个对象。

- cache元素可指定如下属性，每种属性的指定都是针对都是针对底层Cache的一种装饰，采用的是装饰器的模式。
- blocking：默认为false，当指定为true时将采用BlockingCache进行封装，blocking，阻塞的意思，使用BlockingCache会在查询缓存时锁住对应的Key，如果缓存命中了则会释放对应的锁，否则会在查询数据库以后再释放锁，这样可以阻止并发情况下多个线程同时查询数据，详情可参考BlockingCache的源码。 简单理解，也就是设置true时，在进行增删改之后的并发查询，只会有一条去数据库查询，而不会并发
- eviction：eviction，驱逐的意思。也就是元素驱逐算法，默认是LRU，对应的就是LruCache，其默认只保存1024个Key，超出时按照最近最少使用算法进行驱逐，详情请参考LruCache的源码。如果想使用自己的算法，则可以将该值指定为自己的驱逐算法实现类，只需要自己的类实现Mybatis的Cache接口即可。除了LRU以外，系统还提供了FIFO（先进先出，对应FifoCache）、SOFT（采用软引用存储Value，便于垃圾回收，对应SoftCache）和WEAK（采用弱引用存储Value，便于垃圾回收，对应WeakCache）这三种策略。 这里，根据个人需求选择了，没什么要求的话，默认的LRU即可
- flushInterval：清空缓存的时间间隔，单位是毫秒，默认是不会清空的。当指定了该值时会再用ScheduleCache包装一次，其会在每次对缓存进行操作时判断距离最近一次清空缓存的时间是否超过了flushInterval指定的时间，如果超出了，则清空当前的缓存，详情可参考ScheduleCache的实现。
- readOnly：是否只读 默认为false。当指定为false时，底层会用SerializedCache包装一次，其会在写缓存的时候将缓存对象进行序列化，然后在读缓存的时候进行反序列化，这样每次读到的都将是一个新的对象，即使你更改了读取到的结果，也不会影响原来缓存的对象，即非只读，你每次拿到这个缓存结果都可以进行修改，而不会影响原来的缓存结果；当指定为true时那就是每次获取的都是同一个引用，对其修改会影响后续的缓存数据获取，这种情况下是不建议对获取到的缓存结果进行更改，意为只读(不建议设置为true)。 这是Mybatis二级缓存读写和只读的定义，可能与我们通常情况下的只读和读写意义有点不同。每次都进行序列化和反序列化无疑会影响性能，但是这样的缓存结果更安全，不会被随意更改，具体可根据实际情况进行选择。详情可参考SerializedCache的源码。
- size：用来指定缓存中最多保存的Key的数量。其是针对LruCache而言的，LruCache默认只存储最多1024个Key，可通过该属性来改变默认值，当然，如果你通过eviction指定了自己的驱逐算法，同时自己的实现里面也有setSize方法，那么也可以通过cache的size属性给自定义的驱逐算法里面的size赋值。
- type：type属性用来指定当前底层缓存实现类，默认是PerpetualCache，如果我们想使用自定义的Cache，则可以通过该属性来指定，对应的值是我们自定义的Cache的全路径名称。

#### cache-ref元素定义

cache-ref元素可以用来指定其它Mapper.xml中定义的Cache，有的时候可能我们多个不同的Mapper需要共享同一个缓存的，是希望在MapperA中缓存的内容在MapperB中可以直接命中的，这个时候我们就可以考虑使用cache-ref，这种场景只需要保证它们的缓存的Key是一致的即可命中，二级缓存的Key是通过Executor接口的createCacheKey()方法生成的，其实现基本都是BaseExecutor，源码如下。

```java
public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {        
	if (this.closed) {            
		throw new ExecutorException("Executor was closed.");
    } else {
        CacheKey cacheKey = new CacheKey();
        cacheKey.update(ms.getId());
        cacheKey.update(rowBounds.getOffset());
        cacheKey.update(rowBounds.getLimit());
        cacheKey.update(boundSql.getSql());
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        TypeHandlerRegistry typeHandlerRegistry = ms.getConfiguration().getTypeHandlerRegistry();            
        for(int i = 0; i < parameterMappings.size(); ++i) {
            ParameterMapping parameterMapping = (ParameterMapping)parameterMappings.get(i);                
            if (parameterMapping.getMode() != ParameterMode.OUT) {
                String propertyName = parameterMapping.getProperty();
                Object value;                    
                if (boundSql.hasAdditionalParameter(propertyName)) {
                    value = boundSql.getAdditionalParameter(propertyName);
                } else if (parameterObject == null) {
                    value = null;
                } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                    value = parameterObject;
                } else {
                    MetaObject metaObject = this.configuration.newMetaObject(parameterObject);
                    value = metaObject.getValue(propertyName);
                }

                cacheKey.update(value);
            }
        }           
         return cacheKey;
    }
}

```
打个比方我想在MenuMapper.xml中的查询都使用在UserMapper.xml中定义的Cache，则可以通过cache-ref元素的namespace属性指定需要引用的Cache所在的namespace，即UserMapper.xml中的定义的namespace，假设在UserMapper.xml中定义的namespace是cn.chenhaoxiang.dao.UserMapper，则在MenuMapper.xml的cache-ref应该定义如下。
```xml
<cache-ref namespace="cn.chenhaoxiang.dao.UserMapper"/>
```
这样这两个Mapper就共享同一个缓存了

### 二级缓存的使用原则

- 只能在一个命名空间下使用二级缓存  由于二级缓存中的数据是基于namespace的，即不同namespace中的数据互不干扰。在多个namespace中若均存在对同一个表的操作，那么这多个namespace中的数据可能就会出现不一致现象。
- 在单表上使用二级缓存  如果一个表与其它表有关联关系，那么久非常有可能存在多个namespace对同一数据的操作。而不同namespace中的数据互补干扰，所以就有可能出现多个namespace中的数据不一致现象。
- 查询多于修改时使用二级缓存  在查询操作远远多于增删改操作的情况下可以使用二级缓存。因为任何增删改操作都将刷新二级缓存，对二级缓存的频繁刷新将降低系统性能。