package eu.transcriptorium.repository;

import eu.transcriptorium.repository.Repository.ItemProperty;
import eu.transcriptorium.repository.Repository.ItemTest;

public class PropertyCheck implements ItemTest
{
	String operator;
	String referenceValue;
	private ItemProperty itemProperty;

	public PropertyCheck(ItemProperty t, String operator, String referenceValue)
	{
		this.operator = operator;
		this.referenceValue = referenceValue;
		this.itemProperty = t;
	}

	@Override
	public boolean test(Repository r, int id) 
	{
		// TODO Auto-generated method stub
		String v = itemProperty.getPropertyValue(r, id);
		System.err.println(v + " " + referenceValue);
		switch(this.operator)
		{
		case ">": 
			return Double.parseDouble(v) > Double.parseDouble(referenceValue);
		case "<":
			return Double.parseDouble(v) < Double.parseDouble(referenceValue);
		case "=":
			return v.equalsIgnoreCase(referenceValue);
		}
		return false;
	}
}
