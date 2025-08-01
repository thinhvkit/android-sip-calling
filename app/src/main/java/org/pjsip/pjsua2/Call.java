/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua2;

public class Call {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected Call(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(Call obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public boolean wasDestroyed()
  {
    return getCPtr(this) == 0;
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        pjsua2JNI.delete_Call(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected void swigDirectorDisconnect() {
    swigCMemOwn = false;
    delete();
  }

  public void swigReleaseOwnership() {
    swigCMemOwn = false;
    pjsua2JNI.Call_change_ownership(this, swigCPtr, false);
  }

  public void swigTakeOwnership() {
    swigCMemOwn = true;
    pjsua2JNI.Call_change_ownership(this, swigCPtr, true);
  }

  public Call(Account acc, int call_id) {
    this(pjsua2JNI.new_Call__SWIG_0(Account.getCPtr(acc), acc, call_id), true);
    pjsua2JNI.Call_director_connect(this, swigCPtr, swigCMemOwn, true);
  }

  public Call(Account acc) {
    this(pjsua2JNI.new_Call__SWIG_1(Account.getCPtr(acc), acc), true);
    pjsua2JNI.Call_director_connect(this, swigCPtr, swigCMemOwn, true);
  }

  public CallInfo getInfo() throws java.lang.Exception {
    return new CallInfo(pjsua2JNI.Call_getInfo(swigCPtr, this), true);
  }

  public boolean isActive() {
    return pjsua2JNI.Call_isActive(swigCPtr, this);
  }

  public int getId() {
    return pjsua2JNI.Call_getId(swigCPtr, this);
  }

  public static Call lookup(int call_id) {
    long cPtr = pjsua2JNI.Call_lookup(call_id);
    return (cPtr == 0) ? null : new Call(cPtr, false);
  }

  public boolean hasMedia() {
    return pjsua2JNI.Call_hasMedia(swigCPtr, this);
  }

  public Media getMedia(long med_idx) {
    long cPtr = pjsua2JNI.Call_getMedia(swigCPtr, this, med_idx);
    return (cPtr == 0) ? null : new Media(cPtr, false);
  }

  public pjsip_dialog_cap_status remoteHasCap(int htype, String hname, String token) {
    return pjsip_dialog_cap_status.swigToEnum(pjsua2JNI.Call_remoteHasCap(swigCPtr, this, htype, hname, token));
  }

  public void setUserData(SWIGTYPE_p_void user_data) {
    pjsua2JNI.Call_setUserData(swigCPtr, this, SWIGTYPE_p_void.getCPtr(user_data));
  }

  public SWIGTYPE_p_void getUserData() {
    long cPtr = pjsua2JNI.Call_getUserData(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }

  public pj_stun_nat_type getRemNatType() throws java.lang.Exception {
    return pj_stun_nat_type.swigToEnum(pjsua2JNI.Call_getRemNatType(swigCPtr, this));
  }

  public void makeCall(String dst_uri, CallOpParam prm) throws java.lang.Exception {
    pjsua2JNI.Call_makeCall(swigCPtr, this, dst_uri, CallOpParam.getCPtr(prm), prm);
  }

  public void answer(CallOpParam prm) throws java.lang.Exception {
    pjsua2JNI.Call_answer(swigCPtr, this, CallOpParam.getCPtr(prm), prm);
  }

  public void hangup(CallOpParam prm) throws java.lang.Exception {
    pjsua2JNI.Call_hangup(swigCPtr, this, CallOpParam.getCPtr(prm), prm);
  }

  public void setHold(CallOpParam prm) throws java.lang.Exception {
    pjsua2JNI.Call_setHold(swigCPtr, this, CallOpParam.getCPtr(prm), prm);
  }

  public void reinvite(CallOpParam prm) throws java.lang.Exception {
    pjsua2JNI.Call_reinvite(swigCPtr, this, CallOpParam.getCPtr(prm), prm);
  }

  public void update(CallOpParam prm) throws java.lang.Exception {
    pjsua2JNI.Call_update(swigCPtr, this, CallOpParam.getCPtr(prm), prm);
  }

  public void xfer(String dest, CallOpParam prm) throws java.lang.Exception {
    pjsua2JNI.Call_xfer(swigCPtr, this, dest, CallOpParam.getCPtr(prm), prm);
  }

  public void xferReplaces(Call dest_call, CallOpParam prm) throws java.lang.Exception {
    pjsua2JNI.Call_xferReplaces(swigCPtr, this, Call.getCPtr(dest_call), dest_call, CallOpParam.getCPtr(prm), prm);
  }

  public void processRedirect(pjsip_redirect_op cmd) throws java.lang.Exception {
    pjsua2JNI.Call_processRedirect(swigCPtr, this, cmd.swigValue());
  }

  public void dialDtmf(String digits) throws java.lang.Exception {
    pjsua2JNI.Call_dialDtmf(swigCPtr, this, digits);
  }

  public void sendInstantMessage(SendInstantMessageParam prm) throws java.lang.Exception {
    pjsua2JNI.Call_sendInstantMessage(swigCPtr, this, SendInstantMessageParam.getCPtr(prm), prm);
  }

  public void sendTypingIndication(SendTypingIndicationParam prm) throws java.lang.Exception {
    pjsua2JNI.Call_sendTypingIndication(swigCPtr, this, SendTypingIndicationParam.getCPtr(prm), prm);
  }

  public void sendRequest(CallSendRequestParam prm) throws java.lang.Exception {
    pjsua2JNI.Call_sendRequest(swigCPtr, this, CallSendRequestParam.getCPtr(prm), prm);
  }

  public String dump(boolean with_media, String indent) throws java.lang.Exception {
    return pjsua2JNI.Call_dump(swigCPtr, this, with_media, indent);
  }

  public int vidGetStreamIdx() {
    return pjsua2JNI.Call_vidGetStreamIdx(swigCPtr, this);
  }

  public boolean vidStreamIsRunning(int med_idx, pjmedia_dir dir) {
    return pjsua2JNI.Call_vidStreamIsRunning(swigCPtr, this, med_idx, dir.swigValue());
  }

  public void vidSetStream(pjsua_call_vid_strm_op op, CallVidSetStreamParam param) throws java.lang.Exception {
    pjsua2JNI.Call_vidSetStream(swigCPtr, this, op.swigValue(), CallVidSetStreamParam.getCPtr(param), param);
  }

  public StreamInfo getStreamInfo(long med_idx) throws java.lang.Exception {
    return new StreamInfo(pjsua2JNI.Call_getStreamInfo(swigCPtr, this, med_idx), true);
  }

  public StreamStat getStreamStat(long med_idx) throws java.lang.Exception {
    return new StreamStat(pjsua2JNI.Call_getStreamStat(swigCPtr, this, med_idx), true);
  }

  public MediaTransportInfo getMedTransportInfo(long med_idx) throws java.lang.Exception {
    return new MediaTransportInfo(pjsua2JNI.Call_getMedTransportInfo(swigCPtr, this, med_idx), true);
  }

  public void processMediaUpdate(OnCallMediaStateParam prm) {
    pjsua2JNI.Call_processMediaUpdate(swigCPtr, this, OnCallMediaStateParam.getCPtr(prm), prm);
  }

  public void processStateChange(OnCallStateParam prm) {
    pjsua2JNI.Call_processStateChange(swigCPtr, this, OnCallStateParam.getCPtr(prm), prm);
  }

  public void onCallState(OnCallStateParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onCallState(swigCPtr, this, OnCallStateParam.getCPtr(prm), prm); else pjsua2JNI.Call_onCallStateSwigExplicitCall(swigCPtr, this, OnCallStateParam.getCPtr(prm), prm);
  }

  public void onCallTsxState(OnCallTsxStateParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onCallTsxState(swigCPtr, this, OnCallTsxStateParam.getCPtr(prm), prm); else pjsua2JNI.Call_onCallTsxStateSwigExplicitCall(swigCPtr, this, OnCallTsxStateParam.getCPtr(prm), prm);
  }

  public void onCallMediaState(OnCallMediaStateParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onCallMediaState(swigCPtr, this, OnCallMediaStateParam.getCPtr(prm), prm); else pjsua2JNI.Call_onCallMediaStateSwigExplicitCall(swigCPtr, this, OnCallMediaStateParam.getCPtr(prm), prm);
  }

  public void onCallSdpCreated(OnCallSdpCreatedParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onCallSdpCreated(swigCPtr, this, OnCallSdpCreatedParam.getCPtr(prm), prm); else pjsua2JNI.Call_onCallSdpCreatedSwigExplicitCall(swigCPtr, this, OnCallSdpCreatedParam.getCPtr(prm), prm);
  }

  public void onStreamCreated(OnStreamCreatedParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onStreamCreated(swigCPtr, this, OnStreamCreatedParam.getCPtr(prm), prm); else pjsua2JNI.Call_onStreamCreatedSwigExplicitCall(swigCPtr, this, OnStreamCreatedParam.getCPtr(prm), prm);
  }

  public void onStreamDestroyed(OnStreamDestroyedParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onStreamDestroyed(swigCPtr, this, OnStreamDestroyedParam.getCPtr(prm), prm); else pjsua2JNI.Call_onStreamDestroyedSwigExplicitCall(swigCPtr, this, OnStreamDestroyedParam.getCPtr(prm), prm);
  }

  public void onDtmfDigit(OnDtmfDigitParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onDtmfDigit(swigCPtr, this, OnDtmfDigitParam.getCPtr(prm), prm); else pjsua2JNI.Call_onDtmfDigitSwigExplicitCall(swigCPtr, this, OnDtmfDigitParam.getCPtr(prm), prm);
  }

  public void onCallTransferRequest(OnCallTransferRequestParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onCallTransferRequest(swigCPtr, this, OnCallTransferRequestParam.getCPtr(prm), prm); else pjsua2JNI.Call_onCallTransferRequestSwigExplicitCall(swigCPtr, this, OnCallTransferRequestParam.getCPtr(prm), prm);
  }

  public void onCallTransferStatus(OnCallTransferStatusParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onCallTransferStatus(swigCPtr, this, OnCallTransferStatusParam.getCPtr(prm), prm); else pjsua2JNI.Call_onCallTransferStatusSwigExplicitCall(swigCPtr, this, OnCallTransferStatusParam.getCPtr(prm), prm);
  }

  public void onCallReplaceRequest(OnCallReplaceRequestParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onCallReplaceRequest(swigCPtr, this, OnCallReplaceRequestParam.getCPtr(prm), prm); else pjsua2JNI.Call_onCallReplaceRequestSwigExplicitCall(swigCPtr, this, OnCallReplaceRequestParam.getCPtr(prm), prm);
  }

  public void onCallReplaced(OnCallReplacedParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onCallReplaced(swigCPtr, this, OnCallReplacedParam.getCPtr(prm), prm); else pjsua2JNI.Call_onCallReplacedSwigExplicitCall(swigCPtr, this, OnCallReplacedParam.getCPtr(prm), prm);
  }

  public void onCallRxOffer(OnCallRxOfferParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onCallRxOffer(swigCPtr, this, OnCallRxOfferParam.getCPtr(prm), prm); else pjsua2JNI.Call_onCallRxOfferSwigExplicitCall(swigCPtr, this, OnCallRxOfferParam.getCPtr(prm), prm);
  }

  public void onCallTxOffer(OnCallTxOfferParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onCallTxOffer(swigCPtr, this, OnCallTxOfferParam.getCPtr(prm), prm); else pjsua2JNI.Call_onCallTxOfferSwigExplicitCall(swigCPtr, this, OnCallTxOfferParam.getCPtr(prm), prm);
  }

  public void onInstantMessage(OnInstantMessageParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onInstantMessage(swigCPtr, this, OnInstantMessageParam.getCPtr(prm), prm); else pjsua2JNI.Call_onInstantMessageSwigExplicitCall(swigCPtr, this, OnInstantMessageParam.getCPtr(prm), prm);
  }

  public void onInstantMessageStatus(OnInstantMessageStatusParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onInstantMessageStatus(swigCPtr, this, OnInstantMessageStatusParam.getCPtr(prm), prm); else pjsua2JNI.Call_onInstantMessageStatusSwigExplicitCall(swigCPtr, this, OnInstantMessageStatusParam.getCPtr(prm), prm);
  }

  public void onTypingIndication(OnTypingIndicationParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onTypingIndication(swigCPtr, this, OnTypingIndicationParam.getCPtr(prm), prm); else pjsua2JNI.Call_onTypingIndicationSwigExplicitCall(swigCPtr, this, OnTypingIndicationParam.getCPtr(prm), prm);
  }

  public pjsip_redirect_op onCallRedirected(OnCallRedirectedParam prm) {
    return pjsip_redirect_op.swigToEnum((getClass() == Call.class) ? pjsua2JNI.Call_onCallRedirected(swigCPtr, this, OnCallRedirectedParam.getCPtr(prm), prm) : pjsua2JNI.Call_onCallRedirectedSwigExplicitCall(swigCPtr, this, OnCallRedirectedParam.getCPtr(prm), prm));
  }

  public void onCallMediaTransportState(OnCallMediaTransportStateParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onCallMediaTransportState(swigCPtr, this, OnCallMediaTransportStateParam.getCPtr(prm), prm); else pjsua2JNI.Call_onCallMediaTransportStateSwigExplicitCall(swigCPtr, this, OnCallMediaTransportStateParam.getCPtr(prm), prm);
  }

  public void onCallMediaEvent(OnCallMediaEventParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onCallMediaEvent(swigCPtr, this, OnCallMediaEventParam.getCPtr(prm), prm); else pjsua2JNI.Call_onCallMediaEventSwigExplicitCall(swigCPtr, this, OnCallMediaEventParam.getCPtr(prm), prm);
  }

  public void onCreateMediaTransport(OnCreateMediaTransportParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onCreateMediaTransport(swigCPtr, this, OnCreateMediaTransportParam.getCPtr(prm), prm); else pjsua2JNI.Call_onCreateMediaTransportSwigExplicitCall(swigCPtr, this, OnCreateMediaTransportParam.getCPtr(prm), prm);
  }

}
