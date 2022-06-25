package jco.ql.model.command;

import jco.ql.model.GeometryDefinition;
import jco.ql.model.reference.FieldReference;
import jco.ql.parser.model.util.GeometricOption;

public class GeometricOptionCommand {
	private GeometryDefinition geometry;
	private EGeometryAction geometryAction;
	private boolean hasGeometry;
	private String alias;
	
	public GeometricOptionCommand(GeometricOption geometricOption) {
		geometry = null;
		if (geometricOption == null) 
			geometryAction= EGeometryAction.KEEP;
		else {
			if (geometricOption.getType() == GeometricOption.KEEPING) 
				geometryAction = EGeometryAction.KEEP;
			else if (geometricOption.getType() == GeometricOption.DROPPING) 
				geometryAction = EGeometryAction.DROP;
			else if (geometricOption.getType() == GeometricOption.UNDEFINED)  
				geometryAction = EGeometryAction.KEEP;
			else {
				geometry = setGeometryDefinition(geometricOption);
				geometryAction = EGeometryAction.GENERATE;
			}
		} 
		hasGeometry = (geometry != null);
	}

	public GeometryDefinition getGeometry() {
		return geometry;
	}

	public EGeometryAction getGeometryAction() {
		return geometryAction;
	}

	public boolean isHasGeometry() {
		return hasGeometry;
	}

    public String getName () {
    	return "GeometricOptionCommand";
    }

    
	private GeometryDefinition setGeometryDefinition(GeometricOption geometricOption) {
		// ZUN CHECK		
		GeometryDefinition geometryDef = null;

		if (geometricOption.getType() == GeometricOption.FIELD_REF) {
			FieldReference source = new FieldReference(alias + geometricOption.fieldRef.toString());
			geometryDef = new GeometryDefinition(source, GeometryDefinition.FIELD_REF);
		} 
		else if (geometricOption.getType() == GeometricOption.POINT) {
			FieldReference latitude = new FieldReference(alias + geometricOption.latitude.toString());
			FieldReference longitude = new FieldReference(alias + geometricOption.longitude.toString());
			geometryDef = new GeometryDefinition(latitude, longitude);
		} 
		else if (geometricOption.getType() == GeometricOption.AGGREGATE) {
			geometryDef = new GeometryDefinition(
					new FieldReference(alias + geometricOption.aggregate.toString()), 
					GeometryDefinition.AGGREGATE);
		} 
		else if (geometricOption.getType() == GeometricOption.TO_POLYLINE) {
			geometryDef = new GeometryDefinition(
					new FieldReference(alias + geometricOption.fieldRef.toString()), 
					GeometryDefinition.TO_POLYLINE);
		}

		return geometryDef;
	}

}
