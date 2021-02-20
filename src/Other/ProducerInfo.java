package Other;

import java.io.Serializable;

public class ProducerInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public float[] priceTable;
	public boolean[] isAvailable;
	public boolean isRenewable;
	
	public ProducerInfo(){
		this.priceTable = new float[24];
		this.isAvailable = new boolean[24];
		for (int i=0;i<24;i++) this.isAvailable[i] = true;
		this.isRenewable = false;
	}
	
	public ProducerInfo(float[] priceTable,boolean isRenewable){
		this.priceTable = priceTable;
		this.isAvailable = new boolean[24];
		for (int i=0;i<24;i++) this.isAvailable[i] = true;
		this.isRenewable = isRenewable;
	}
	
	public String toString() {
		String s = "";
		for (int i=0;i<24;i++) s += priceTable[i] + " ";
		return (isRenewable ? "Renewable" : "Non-Renewable") + "|Prices : " + s;
	}
}
