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
