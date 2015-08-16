/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\github\\phoneadapter\\src\\android\\view\\IWindowManager.aidl
 */
package android.view;
/**
 * System private interface to the window manager.
 *
 * {@hide}
 */
public interface IWindowManager extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements android.view.IWindowManager
{
private static final java.lang.String DESCRIPTOR = "android.view.IWindowManager";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an android.view.IWindowManager interface,
 * generating a proxy if needed.
 */
public static android.view.IWindowManager asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof android.view.IWindowManager))) {
return ((android.view.IWindowManager)iin);
}
return new android.view.IWindowManager.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_injectKeyEvent:
{
data.enforceInterface(DESCRIPTOR);
android.view.KeyEvent _arg0;
if ((0!=data.readInt())) {
_arg0 = android.view.KeyEvent.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
boolean _arg1;
_arg1 = (0!=data.readInt());
boolean _result = this.injectKeyEvent(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_injectPointerEvent:
{
data.enforceInterface(DESCRIPTOR);
android.view.MotionEvent _arg0;
if ((0!=data.readInt())) {
_arg0 = android.view.MotionEvent.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
boolean _arg1;
_arg1 = (0!=data.readInt());
boolean _result = this.injectPointerEvent(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_injectTrackballEvent:
{
data.enforceInterface(DESCRIPTOR);
android.view.MotionEvent _arg0;
if ((0!=data.readInt())) {
_arg0 = android.view.MotionEvent.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
boolean _arg1;
_arg1 = (0!=data.readInt());
boolean _result = this.injectTrackballEvent(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements android.view.IWindowManager
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public boolean injectKeyEvent(android.view.KeyEvent ev, boolean sync) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((ev!=null)) {
_data.writeInt(1);
ev.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeInt(((sync)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_injectKeyEvent, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean injectPointerEvent(android.view.MotionEvent ev, boolean sync) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((ev!=null)) {
_data.writeInt(1);
ev.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeInt(((sync)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_injectPointerEvent, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean injectTrackballEvent(android.view.MotionEvent ev, boolean sync) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((ev!=null)) {
_data.writeInt(1);
ev.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeInt(((sync)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_injectTrackballEvent, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_injectKeyEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_injectPointerEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_injectTrackballEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
public boolean injectKeyEvent(android.view.KeyEvent ev, boolean sync) throws android.os.RemoteException;
public boolean injectPointerEvent(android.view.MotionEvent ev, boolean sync) throws android.os.RemoteException;
public boolean injectTrackballEvent(android.view.MotionEvent ev, boolean sync) throws android.os.RemoteException;
}
