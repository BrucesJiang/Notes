package reverselist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class ReverseList<T> extends ArrayList<T>{


	private static final long serialVersionUID = 1L;
	
	
	public ReverseList(Collection<T> c){super(c);}
	
	/**
	 * 功能： 实现反向迭代，以 空格为分割符
	 * @return
	 */
	public Iterable<T> revered() {
		return new Iterable<T>() {

			@Override
			public Iterator<T> iterator() {
				return new Iterator<T>(){

					@Override
					public boolean hasNext() {
						return this.hasNext();
					}

					@Override
					public T next() {
						return this.next();
					}
					
				};
			}
			
		};
	}
	

}
