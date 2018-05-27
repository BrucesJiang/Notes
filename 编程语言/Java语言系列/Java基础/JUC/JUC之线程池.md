# Java�̳߳�
����ʹ���̳߳ص����ƣ�
1. ������Դ���ġ� ͨ���ظ������Ѵ������߳̽����̴߳�����������ɵ����ġ�
2. �����Ӧ�ٶȡ� �����񵽴�ʱ��������Բ���Ҫ�ȵ��̴߳�����������ִ�С�
3. �ṩ�̵߳Ŀɹ����ԡ� �߳���ϡȱ��Դ����������ƵĴ���������������ϵͳ��Դ�����ή��ϵͳ���ȶ��ԣ�ʹ���̳߳ؿ��Խ���ͳһ���䡢���źͼ�ء�

## �̳߳ص�ʵ��ԭ��
�����̳߳��ύһ��������̳߳صĴ����������£�
1. �̳߳��жϺ����̳߳�����߳��Ƿ���ִ������������ǣ��򴴽�һ���µĹ������߳���ִ��������������̳߳�����̶߳���ִ�������������һ�����̡�
2. �̳߳��жϹ��������Ƿ������������������û�����������ύ������洢��������������������������������������һ�����̡�
3. �̳߳��ж��̳߳ص��߳��Ƿ񶼴��ڹ���״̬�����û�У��򴴽�һ���µĹ����߳���ִ����������Ѿ����ˣ��򽻸����Ͳ����������������

�̳߳ص���Ҫ�������̣�

![executor_pool_workflow](./images/executor_pool_workflow.png)


`ThreadPoolExecutor`ִ��`execute()`������ʾ��ͼ��

![threadPoolExecutor_workflow](./images/threadPoolExecutor_workflow.png)

`ThreadPoolExecutor`ִ��`execute()`������Ϊ��������������
1. �����ǰ���е��߳�����`corePoolSize`���򴴽����߳���ִ������ע�⣬ִ����һ������Ҫ��ȡȫ������
2. ������е��̵߳��ڻ����`corePoolSize`�����������`BlockingQueue`
3. ����޷����������`BlockingQueue`���������������򴴽��µ��߳�����������ע�⣬ִ����һ����Ҫ��ȡȫ������
4. ����������߳̽�ʹ��ǰ���е��̳߳���`maximumPoolSize`,���񽫻ᱻ�ܾ���������`RejectedExceptionHandler.rejectedExecution()`������

`ThreadPoolExecutor`��ȡ����������������˼·��Ϊ��ִ��`execute()`����ʱ�������ܱ����ȡȫ��������`ThreadPoolExecutor`���Ԥ��֮�󣨵�ǰ�����̴߳��ڵ���corePoolSize)���������е�`execute()`�������ö���ִ�в���2��������2����Ҫ��ȡȫ������

`execute`Դ�룺

```java
public void execute(Runnable command) {
	if(comman == null) throw new NullPointerException();
	//����߳��������ڻ����߳������򴴽��̲߳�ִ�е�ǰ����
	if(poolSize >= corePoolSize || !addIfUnderCorePoolSize(command)) {
		//����߳������ڻ����߳������̴߳���ʧ�ܣ��򽫵�ǰ����ŵ�����������
		if(runState == RUNNING && workQueue.offer(command)) {
			if(runState != RUNNING || poolSize == 0) {
				ensureQueuedTaskHandled(command);
			}
		}
		//����̳߳ز����������л������޷�������У��ҵ�ǰ�߳�����С�����������߳��������򴴽�һ���߳�ִ������
		else if (!addIfUnderMaximumPoolSize(command)) {
			//�׳�RejectedExecutionException�쳣
			reject(command); //is shutdown or saturated
		}
	}
}
```

**�����߳�** �̳߳ش����߳�ʱ���Ὣ�̷߳�װ�ɹ����߳�Worker��Worker��ִ��������󣬻���ѭ����ȡ�����������������ִ�С�

```java
public void run() {
	try{
		Runnable task = firstTask;
		firstTask = null;
		while(task != null || (task = getTask()) != null) {
			runTask(task);
			task = null;
		}
	}finally {
		workerDone(this);
	}
}

```
`ThreadPoolExecutor`���߳�ִ�������ʾ��ͼ��

![threadPoolExecutor_workflow_1](./images/threadPoolExecutor_workflow_1.png)

�̳߳��е��߳�ִ����������������
1. ��`execute()`��������һ���߳�ʱ����������߳�ִ�е�ǰ����
2. ����߳�ִ����������󣬻ᷴ����`BlockingQueue`��ȡ������ִ�С�

## �̳߳ص�ʹ��
### �̳߳صĴ���
����ͨ��`ThreadPoolExecutor`������һ���̳߳ء�
```java
new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, milliseconds, runnableTaskQueue, handler);
```

�����̳߳�ʱ��Ҫ����������

1. `corePoolSize`���̳߳صĻ�����С�������ύһ�������̳߳�ʱ���̳߳ػᴴ��һ���߳���ִ�����񣬼�ʹ�������еĻ����߳��ܹ�ִ��������Ҳ�ᴴ���̣߳��ȵ���Ҫִ�е������������̳߳ػ�����Сʱ�Ͳ��ٴ���������������̳߳ص�`prestartAllCoreThreads()`�������̳߳ػ���ǰ�������������л����̡߳�
2. `runnableTaskQueue`��������У��� ���ڱ���ȴ�ִ�е�������������С�����ѡ�����¼������У�
	- `ArrayBlockingQueue` һ����������ṹ���н��������У��ö��а���FIFOԭ���Ԫ�ؽ�������
	- `LinkedBlockingQueue` һ����������ṹ���������У��ö��а�FIFO����Ԫ�أ�������Ҫ����`ArrayBlockingQueue`����̬��������`Executors.newFixedThreadPool()`��
	- `SynchronousQueue`��һ�����ܴ洢Ԫ�ص��������С�ÿ�������������ȴ���һ���̵߳����Ƴ�����������������һֱ��������״̬��������Ҫ����`LinkedBlockingQueue`����̬��������`Executors.newCachedThreadPool`ʹ����������С�
	- `PriorityBlockingQueue`�� һ���������ȼ��������������С�
3. `maximumPoolSize`���̳߳�������������̳߳�������������߳�����������������ˣ������Ѿ��������߳���С������߳��������̳߳ػ��ڴ����µ��߳�ִ������ֵ��ע����ǣ����ʹ�����޽������������������û��Ч����
4. `ThreadFactory`���������ô����̵߳Ĺ������Ϳ���ͨ���̹߳�����ÿ�������������߳����ø�����������֡�ʹ�ÿ�Դ��guava�ṩ��`ThreadFactoryBuilder`���Կ��ٸ��̳߳�����߳���������������֡����磺

```java
new ThreadFactoryBuilder().setNameFormat("xx-task-%d").build();
```

5. `RetectedExecutionHandler`�����Ͳ��ԣ��������к��̳߳ض�����˵���̳߳ش��ڱ���״̬����ô�����ȡһ�ֲ��Դ����ύ���������������Ĭ���������`AbortPolicy`����ʾ�޷�����������ʱ�׳����쳣����JDK 1.5���̳߳ؿ���ṩ���������ֲ��ԣ�

- AbortPolicy: ֱ���׳��쳣
- CallerRunsPolicy: ֻ�õ��������ڵ��߳���������������
- DiscardOldestPolicy: �������������µ�һ�����񣬲�ִ�е�ǰ����
- DiscardPolicy: ��������������

Ҳ���Ը��ݳ�����Ҫʵ��`RejectedExecutionHandler`�ӿ��Զ�����ԡ����¼��־��־û����ܴ��������
- keeyAliveTime���̻߳����ʱ�䣩�� �̳߳صĹ����߳����ú󣬱��ִ���ʱ�䡣��������ܶ࣬����ÿ������ִ�е�֮��϶̣����Ե���ʱ�䣬����̵߳������ʡ�
- TimeUnit���̻߳������ѡ��λ���죨DAYS����Сʱ��HOURS�������ӣ�MINUTES�������루MILLISECONDS����΢�루MICROSECONDS��ǧ��֮һ���룩�����루NANOSECONDS��ǧ��֮һ΢�룩

## ���߳��ύ����
�����ַ������߳��ύ���񣬷ֱ�Ϊ`execute()`��`submit()`������

`execute()`���������ύ����Ҫ����ֵ�����������޷��ж������Ƿ��̳߳�ִ�гɹ���ͨ�����´����֪`execute()`���������������һ��`Runnable`���ʵ����

```java
threadPool.execute(new Runnable() {
	@Override public void run(){
		//TODO Auto-generated method stub
	}
});
```

`submit()`���������ύ��Ҫ����ֵ�������̳߳ػ᷵��һ��`Future`���͵Ķ���ͨ�����`Future`��������ж������Ƿ�ִ�гɹ������ҿ���ͨ��`Future`��`get()`��������ȡ����ֵ��`get()`������������ǰ�߳�ֱ��������ɣ���ʹ��`get(long timeout, TimeUnit unit)`�������������ǰ�߳�һ��ʱ����������ء�

```java
Future<Object> future = executor.submit(harReturnValueTask);
try{
	Object s = future.get();
}catch(InterruptedException e) {
	//�����ж��쳣
}catch(ExecutionException e) {
	//�����޷�ִ�������쳣
	

}finally{
	//�ر��̳߳�
	executor.shutdown();
}
```

## �ر��̳߳�
ͨ�������̳߳ص�`shutdown()`��`shutdownNow()`�������ر��̳߳ء����ǵ�ԭ���Ǳ����̳߳��еĹ����̣߳�Ȼ����������̵߳�`interrupt`�������ж��̣߳������޷���Ӧ�жϵ����������Զ�޷���ֹ���������Ǵ� ��һ������`shutdownNow()`���Ƚ��̳߳ص�״̬���ó�`STOP`��Ȼ����ֹͣ��������ִ�л���ͣ���̣߳������صȴ�ִ��������б���`shutdown`ֻ�ǽ��̳߳ص�״̬���ó�`SHUTDOWN`״̬��Ȼ���ж���������״̬���̡߳�ֻҪ�������������رշ���������һ����`isShutDown`�����ͻ᷵��`true`�������������ѹرպ󣬲ű�ʾ�̹߳رճɹ�����ʱ����`isTerminaed`�����᷵��`true`��������һ�ַ������ر��̳߳أ�Ӧ�����ύ���������Ծ�����ͨ������`shutdown`�������ر��̳߳أ��������һ��Ҫ��ɣ����Ե���`shotdownNow()`��

## ���������̳߳�
�Ӽ����Ƕȷ�����

- �������ʣ� CPU�ܼ�������IO�ܼ�������ͻ��������
- ��������ȼ��� �ߡ��к͵�
- �����ִ��ʱ�䣺 �����кͶ�
- ����������ԣ��Ƿ���������ϵͳ��Դ���������ݿ�����

���ʲ�ͬ����������ò�ͬ��ģ���̳߳طֿ�����CPU�ܼ�������Ӧ���þ������ٵ��̣߳�������N(cpu)+1���̵߳��̳߳ء�����IO�ܼ��������̲߳�����һֱ��ִ��������Ӧ�����þ����ܶ���̣߳�����2*N(cpu)���̡߳� ���������������Բ�֣������ֳ�һ��CPU�ܼ��������һ��IO�ܼ������񡣿���ͨ�� `Runtime.getRuntime().availableProcessors()`������õ�ǰ�豸��CPU������

## �̳߳صļ��
- `taskCount` �̳߳���Ҫִ�е���������
- `completedTaskCount`�̳߳������й������Ѿ���ɵ�����������С�ڻ����`taskCount`
- `largestPoolSize` �̳߳�������������������߳�������ͨ��������ݿ���֪���̳߳��Ƿ������������������ֵ�����̳߳ص�����С�����ʾ�̳߳�����������
- `getPoolSize` �̳߳ص��߳�����������̳߳ز����ٵĻ����̳߳�����̲߳����Զ����٣��������ֵֻ��������
- `getActiveCount`�� ��ȡ����߳�����