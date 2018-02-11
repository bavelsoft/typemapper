package test;

import com.bavelsoft.typemapper.TypeMap;
import com.bavelsoft.typemapper.Field;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class FieldTest {
	public interface Foo {
		@TypeMap
		@Field(source="source.getY()", target="setX()")
		MyTarget f(MySource source);
	}

	@Test public void test()  {
		Foo mapper = new FieldTest_FooTypeMapper();
		MyTarget target = mapper.f(new MySource(123, 456));
		assertEquals(456, target.x);
	}
}
