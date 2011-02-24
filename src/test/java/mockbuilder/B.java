package mockbuilder;

import java.util.List;
import java.util.Map;

interface B {
	C getC();
	C[] getCa();
	List<C> getCl();
	Map<String, C> getCmap();
	Map<Long, C> getCmapLong();
	E getE();
}
