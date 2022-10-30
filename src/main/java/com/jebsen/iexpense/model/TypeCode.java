package com.jebsen.iexpense.model;

import lombok.Getter;
import lombok.Setter;

import java.sql.SQLData;
import java.sql.SQLException;
import java.sql.SQLInput;
import java.sql.SQLOutput;

@Getter
@Setter
public class TypeCode implements SQLData {

    private String DFF_TYPE;
    private String DFF_CODE;
    private String DFF_STATUS;

    @Override
    public String getSQLTypeName() throws SQLException {
        return "JCL_DFF_OBJ";
    }

    @Override
    public void readSQL(SQLInput stream, String typeName) throws SQLException {
        setDFF_TYPE(stream.readString());
        setDFF_CODE(stream.readString());
        setDFF_STATUS(stream.readString());
    }

    @Override
    public void writeSQL(SQLOutput stream) throws SQLException {
        stream.writeString(getDFF_TYPE());
        stream.writeString(getDFF_CODE());
        stream.writeString(getDFF_STATUS());
    }
}
