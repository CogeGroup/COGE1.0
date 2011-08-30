package utility;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ConvertToJson {
	
	public static String convert(List l,String propertyValue, String propertyLabel ){
		
		//List<DomainWrapper> ret = new ArrayList<DomainWrapper>();
		StringBuffer sb = new StringBuffer("[");
		
		for (Object o : l){
			
			Class c = o.getClass();
			try {
				Field mValue = c.getField(propertyValue);
				Field mLabel = c.getField(propertyLabel);
				//ret.add(new DomainWrapper(Integer.parseInt(mValue.get(o).toString()), mLabel.get(o).toString()));
				sb.append("{");
				sb.append("'value':");
				sb.append(Integer.parseInt(mValue.get(o).toString()));
				sb.append(",");
				
				sb.append("'label':");
				sb.append("'" +  mLabel.get(o).toString() + "'");
				
				sb.append("},");
				
			} catch (Exception e) {
				e.printStackTrace();
			} 
			
			
			
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append( "]" );
		return sb.toString();
	}

}
