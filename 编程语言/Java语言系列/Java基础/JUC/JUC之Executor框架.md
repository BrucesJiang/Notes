# JUC֮Executor���
��Java�У�ʹ���߳����첽ִ������Java�̵߳Ĵ�����������Ҫһ���Ŀ������������Ϊÿһ�����񴴽�һ�����߳���ִ�У���Щ�̵߳Ĵ��������ٽ����Ĵ����ļ�����Դ��ͬʱ��Ϊÿ�����񴴽�һ�����߳���ִ�У����ֲ��Ժܿ��ܻ�ʹ���ڸ߸���״̬��Ӧ�����ձ�����Java�̼߳��ǹ�����Ԫ��Ҳ��ִ�л��ơ���Java 5��ʼ��Java��Ŀ����ǰѹ�����Ԫ��ִ�л��Ʒ��롣������Ԫ����Runnable��Callable����ִ�л�����Executor����ṩ��

## Executor��ܵ���������ģ��
��HotSpot VM���߳�ģ���У�Java�̣߳�java.land.Thread)��һ��һӳ��Ϊ���ز���ϵͳ�̡߳�Java�߳�����ʱ�ᴴ��һ�����ز���ϵͳ�̣߳�����Java�̱߳���ֹʱ���������ϵͳ�߳�Ҳ�ᱻ���ա�����ϵͳ����������̲߳������Ƿ�������õ�CPU��

���ϲ㣬Java���̳߳���ͨ�����Ӧ�÷ֽ�Ϊ���ɸ�����Ȼ��ʹ���û����ĵ�������Executor��ܣ�����Щ����ӳ��Ϊ�̶��������̣߳��ڵײ㣬����ϵͳ�ں˽���Щ�߳�ӳ�䵽Ӳ���������ϡ�������������ģ�͵�ʾ��ͼ������ͼ��ʾ��

![two_aspects_model](./images/two_aspects_model.png)

��ͼ�п��Կ�����Ӧ�ó���ͨ��Executor��ܿ����ϲ�ĵ��ȣ����²�ĵ����ɲ���ϵͳ�ں˿��ƣ��²�ĵ��Ȳ����յ�Ӧ�ó���Ŀ��ơ�


## Executor��ܵĽṹ�ͳ�Ա
���Ľ�Executor��Ϊ��������������Executor: `Executor`�Ľṹ��`Executor`��ܰ����ĳ�Ա�����

### Executor��ܵĽṹ
Executor��ܴ��¿��Է�Ϊ�������֣�

- ����  ������ִ��������Ҫʵ�ֵĽӿڣ�`Runnable`�ӿڻ�`Callable`�ӿ�
- �����ִ��  ��������ִ�л��Ƶĺ��Ľӿ�`Executor`���Լ��̳���`Executor`��`ExecutorService`�ӿڡ� `Executor`����������ؼ���`ThreadPoolExecutor`��`ScheduledThreadPoolExecutor`ʵ����`ExecutorService`�ӿڡ�
- �첽����Ľ���� �����ӿ�`Future`��ʵ��`Future`�ӿڵ�`FutureTask`�ࡣ

Executor��ܰ�������Ҫ����ͽӿڣ�����ͼ��ʾ��

![executor_framework_class_interface](./images/executor_framework_class_interface.png)

- Executor��һ���ӿڡ�



![how_to_use_executor](./images/how_to_use_executor.png)