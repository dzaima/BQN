package APL.types.functions.builtins.mops;

import APL.errors.NYIError;
import APL.types.*;
import APL.types.functions.*;

public class InvBuiltin extends Mop {
  
  public Value call(Value f, Value x, DerivedMop derv) {
    return f.asFun().callInv(x);
  }
  public Value call(Value f, Value w, Value x, DerivedMop derv) {
    return f.asFun().callInvW(w, x);
  }
  public Value callInvW(Value f, Value w, Value x) {
    return f.asFun().call(w, x);
  }
  public Value callInvA(Value f, Value w, Value x) {
    throw new NYIError("⁼ inverting 𝕨", this);
  }
  
  public String repr() {
    return "⁼";
  }
  
  
  
  
  public static Fun invertM(Fun f) {
    return new Fun() {
      public String repr() { return f.repr()+"⁼"; }
      public Value call(Value x) {
        return f.callInv(x);
      }
    };
  }
  
  public static Fun invertW(Fun f) {
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
  
  public static Fun invertA(Fun f) {
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