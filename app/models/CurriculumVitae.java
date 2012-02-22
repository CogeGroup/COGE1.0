package models;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import play.db.jpa.GenericModel;


@javax.persistence.Entity
public class CurriculumVitae extends GenericModel {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer idCurriculumVitae;
	
	public String fileName;
	
	public Blob fileBlob;
	
	@Transient
	public byte[] fileBytes;
	
	public CurriculumVitae() {}

	@Override
	public String toString() {
		return fileName;
	}
	
	public void createBlob(){
		try {
			Class.forName("com.mysql.jdbc.Driver"); 
		    String url = "jdbc:mysql://localhost:3306/coge"; 
		    Connection conn = DriverManager.getConnection(url,"root","root");  
			this.fileBlob = conn.createBlob();
		this.fileBlob.setBytes(1, this.fileBytes);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	} 
	
	public InputStream getBinaryStream() {
		try {
			return this.fileBlob.getBinaryStream();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void createBytes() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    	byte[] buf = new byte[1024];
	    	InputStream in = this.fileBlob.getBinaryStream();
	    	int n = 0;
			while ((n=in.read(buf))>=0){
				baos.write(buf, 0, n);
			}
	    	in.close();
	    	this.fileBytes = baos.toByteArray();
    	} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
