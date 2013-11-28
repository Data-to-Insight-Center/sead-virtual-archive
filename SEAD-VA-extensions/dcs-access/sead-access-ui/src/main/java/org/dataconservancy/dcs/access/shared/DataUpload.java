package org.dataconservancy.dcs.access.shared;


public class DataUpload implements java.io.Serializable{
	private String datasetId;
	private String sipId;
	private String datauploadId;
	
	private String dataUploadStatus;
	

    public DataUpload() {

    }


	/**
	 * @return the datasetId
	 */
	public String getDatasetId() {
		return datasetId;
	}


	/**
	 * @param datasetId the datasetId to set
	 */
	public void setDatasetId(String datasetId) {
		this.datasetId = datasetId;
	}


	/**
	 * @return the sipId
	 */
	public String getSipId() {
		return sipId;
	}


	/**
	 * @param sipId the sipId to set
	 */
	public void setSipId(String sipId) {
		this.sipId = sipId;
	}


	/**
	 * @return the datauploadId
	 */
	public String getDatauploadId() {
		return datauploadId;
	}


	/**
	 * @param datauploadId the datauploadId to set
	 */
	public void setDatauploadId(String datauploadId) {
		this.datauploadId = datauploadId;
	}


	/**
	 * @return the dataUploadStatus
	 */
	public String getDataUploadStatus() {
		return dataUploadStatus;
	}


	/**
	 * @param dataUploadStatus the dataUploadStatus to set
	 */
	public void setDataUploadStatus(String dataUploadStatus) {
		this.dataUploadStatus = dataUploadStatus;
	}
    
    
}
