package APL.tools;

import java.util.Arrays;

public class MutByteArr { 
  public byte[] bs;
  public int len;
  
  public MutByteArr() {
    bs = new byte[20];
  }
  public MutByteArr(int cap) {
    bs = new byte[cap];
  }
  private void dbl() {
    bs = Arrays.copyOf(bs, bs.length*2);
  }
  
  public void u(int v) {
    assert v>=0 && v<256;
    if (len>=bs.length) dbl();
    bs[len++] = (byte) v;
  }
  public void u(byte... v) {
    while (len+v.length>=bs.length) dbl();
    System.arraycopy(v,0,bs,len,v.length);
    len+= v.length;
  }
  public void u(int... v) {
    while (len+v.length>=bs.length) dbl();
    for (int i = 0; i < v.length; i++) {
      int c = v[i];
      assert c>=0 && c<255;
      bs[i+len] = (byte) c;
    }
    len+= v.length;
  }
  public void u2(int v) {
    assert v>=0 && v<65536;
    u((v>>8)&0xff);
    u( v    &0xff);
  }
  public void u4(int v) {
    u((v>>24)&0xff);
    u((v>>16)&0xff);
    u((v>> 8)&0xff);
    u( v     &0xff);
  }
  
  public void u(byte[] v, int s, int e) {
    while (len+e>=bs.length) dbl();
    System.arraycopy(v, s, bs, len, e-s);
    len+= e-s;
  }
  
  public void s(byte v) {
    if (len>=bs.length) dbl();
    bs[len++] = v;
  }
  public void s(int v) {
    assert (byte)v == v;
    if (len>=bs.length) dbl();
    bs[len++] = (byte) v;
  }
  public void s2(int v) {
    assert (short)v == v;
    u((v>>8)&0xff);
    u( v    &0xff);
  }
  
  public byte[] get() {
    return Arrays.copyOf(bs, len);
  }
}