package APL.types.callable.builtins.mops;

import APL.errors.NYIError;
import APL.types.*;
import APL.types.callable.DerivedMop;
import APL.types.callable.builtins.MopBuiltin;

public class InvBuiltin extends MopBuiltin {
  public String repr() {
    return "⁼";
  }
  
  public Value call(Value f, Value x, DerivedMop derv) {
    return f.callInv(x);
  }
  public Value call(Value f, Value w, Value x, DerivedMop derv) {
    return f.callInvX(w, x);
  }
  
  public Value callInv(Value f, Value x) {
    return f.call(x);
  }
  public Value callInvX(Value f, Value w, Value x) {
    return f.call(w, x);
  }
  public Value callInvW(Value f, Value w, Value x) {
    throw new NYIError("⁼ inverting 𝕨", this);
  }
  
  
  
  
  public static Fun invertM(Value f) {
    return new Fun() {
      public String repr() { return f.repr()+"⁼"; }
      public Value call(Value x) {
        return f.callInv(x);
      }
    };
  }
  
  public static Fun invertW(Value f) {
    return new Fun() {
      public String repr() { return f.repr()+"⁼"; }
      public Value call(Value w, Value x) {
        return f.callInvX(w, x);
      }
      
      public Value callInvX(Value w, Value x) {
        return f.call(w, x);
      }
    };
  }
  
  public static Fun invertA(Value f) {
    return new Fun() {
      public String repr() { return f.repr()+"˜⁼˜"; }
      public Value call(Value w, Value x) {
        return f.callInvW(w, x);
      }
      
      public Value callInvW(Value w, Value x) {
        return f.call(w, x);
      }
    };
  }
}