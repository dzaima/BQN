package APL.types.functions.builtins.mops;

import APL.errors.*;
import APL.types.*;
import APL.types.functions.*;

public class InvBuiltin extends Mop {
  public String repr() {
    return "⁼";
  }
  
  public Value call(Value f, Value x, DerivedMop derv) {
    return f.callInv(x);
  }
  public Value call(Value f, Value w, Value x, DerivedMop derv) {
    return f.callInvW(w, x);
  }
  
  public Value callInv(Value f, Value x) {
    return f.call(x);
  }
  public Value callInvW(Value f, Value w, Value x) {
    return f.call(w, x);
  }
  public Value callInvA(Value f, Value w, Value x) {
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
        return f.callInvW(w, x);
      }
      
      public Value callInvW(Value w, Value x) {
        return f.call(w, x);
      }
    };
  }
  
  public static Fun invertA(Value f) {
    return new Fun() {
      public String repr() { return f.repr()+"˜⁼˜"; }
      public Value call(Value w, Value x) {
        return f.callInvA(w, x);
      }
      
      public Value callInvA(Value w, Value x) {
        return f.call(w, x);
      }
    };
  }
}