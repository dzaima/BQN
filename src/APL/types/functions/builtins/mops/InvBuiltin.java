package APL.types.functions.builtins.mops;

import APL.errors.NYIError;
import APL.types.*;
import APL.types.functions.*;

public class InvBuiltin extends Mop {
  
  public Value call(Value f, Value w, DerivedMop derv) {
    return f.asFun().callInv(w);
  }
  public Value call(Value f, Value a, Value w, DerivedMop derv) {
    return f.asFun().callInvW(a, w);
  }
  public Value callInvW(Value f, Value a, Value w) {
    return f.asFun().call(a, w);
  }
  public Value callInvA(Value f, Value a, Value w) {
    throw new NYIError("⁼ inverting 𝕨", this);
  }
  
  public String repr() {
    return "⁼";
  }
  
  
  
  
  public static Fun invertM(Fun f) {
    return new Fun() {
      public String repr() { return f.repr()+"⁼"; }
      public Value call(Value w) {
        return f.callInv(w);
      }
    };
  }
  
  public static Fun invertW(Fun f) {
    return new Fun() {
      public String repr() { return f.repr()+"⁼"; }
      public Value call(Value a, Value w) {
        return f.callInvW(a, w);
      }
      
      public Value callInvW(Value a, Value w) {
        return f.call(a, w);
      }
    };
  }
  
  public static Fun invertA(Fun f) {
    return new Fun() {
      public String repr() { return f.repr()+"˜⁼˜"; }
      public Value call(Value a, Value w) {
        return f.callInvA(a, w);
      }
      
      public Value callInvA(Value a, Value w) {
        return f.call(a, w);
      }
    };
  }
}