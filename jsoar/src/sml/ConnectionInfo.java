/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.35
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package sml;

public class ConnectionInfo {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected ConnectionInfo(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(ConnectionInfo obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if(swigCPtr != 0 && swigCMemOwn) {
      swigCMemOwn = false;
      smlJNI.delete_ConnectionInfo(swigCPtr);
    }
    swigCPtr = 0;
  }

  public ConnectionInfo(String pID, String pName, String pStatus, String pAgentStatus) {
    this(smlJNI.new_ConnectionInfo(pID, pName, pStatus, pAgentStatus), true);
  }

  public String GetID() {
    return smlJNI.ConnectionInfo_GetID(swigCPtr, this);
  }

  public String GetName() {
    return smlJNI.ConnectionInfo_GetName(swigCPtr, this);
  }

  public String GetConnectionStatus() {
    return smlJNI.ConnectionInfo_GetConnectionStatus(swigCPtr, this);
  }

  public String GetAgentStatus() {
    return smlJNI.ConnectionInfo_GetAgentStatus(swigCPtr, this);
  }

}