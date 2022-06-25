package jco.ql.model;
 
import java.util.List;

import jco.ql.model.reference.FieldReference;
 
public class GeometryDefinition {
    private FieldReference latitude;
    private FieldReference longitude;
    private FieldReference field;
    private List<FieldReference> fieldList;
    private int  type;
    
    public static final int POINT = 0; 
    public static final int AGGREGATE = 1; 
    public static final int FIELD_REF = 2; 
    public static final int TO_POLYLINE = 3; 
    
    public GeometryDefinition(FieldReference latitude, FieldReference longitude) {
        this.type=0;
        this.latitude = latitude;
        this.longitude = longitude;
    }
 
    public GeometryDefinition(FieldReference field, int type) {
        this.type=type;
        this.field = field;
    }
      
    public int getType(){
        return this.type;
    }
   
    public FieldReference getLatitude(){
        return this.latitude;
    }
   
    public FieldReference getLongitude(){
        return this.longitude;
    }
   
    public FieldReference getField(){
        return this.field;
    }
    public List<FieldReference>  getFieldList(){
        return this.fieldList;
    }
}