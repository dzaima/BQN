package BQN.tools;

import BQN.*;
import BQN.errors.*;
import BQN.tokenizer.types.BlockTok;
import BQN.types.*;
import BQN.types.callable.builtins.fns.EvalBuiltin;
import BQN.types.callable.trains.*;
import BQN.types.mut.*;

import java.util.*;

import static BQN.Comp.*;

public class JBQNComp extends JBC {
  public final JFn r;
  public JBQNComp(Comp comp, int start) {
    
    {
      Met fn = new Met(0x1001, "<init>", "()V", 1);
      fn.aload(0);
      fn.invspec(JFn.class, "<init>", "()V");
      fn.vret();
      fn.mstack = 1;
      methods.add(fn);
    }
    
    {
      
      
      Met fn = new Met(0x0001, "get", met(Value.class, Scope.class, Body.class), 3);
      // aload 0 - this (JFn); 1 - scope; 2 - body; 3,4,5 - temp values
      int SC   = 1;
      int BODY = 2;
      int TMP  = 3;
      int TMP2 = 4;
      int TMP3 = 5;
      // fields.add(new Fld(0x0009, "vals", name(Value[].class))); // ACC_PUBLIC ACC_STATIC ACC_FINAL
      
      
      int mstack = 0;
      
      
      int[] bc = comp.bc;
      int i = start;
      int cstack=0;
      loop: while (i != bc.length) {
        int pi = i;
        i++;
        switch (bc[pi]) { default: throw new DomainError("Unimplemented bytecode "+bc[pi]);
          case PUSH: {
            int n = bc[i++];
            fn.aload(0);
            fn.getfield(JFn.class, "vals", Value[].class);
            fn.iconst(n);
            fn.aaload();
            mstack = Math.max(mstack, cstack+2);
            cstack++;
            break;
          }
          
          case VARO: {
            int n = bc[i++];
            fn.aload(SC);
            fn.ldc(comp.objs[n].asString());
            fn.invvirt(Scope.class, "getC", met(Value.class, String.class));
            mstack = Math.max(mstack, cstack+4);
            cstack++;
            break;
          }
          case VARM: {
            int n = bc[i++];
            fn.new_(Variable.class); fn.dup();
            fn.ldc(comp.objs[n].asString());
            fn.invspec(Variable.class, "<init>", met(void.class, String.class));
            mstack = Math.max(mstack, cstack+3);
            cstack++;
            break;
          }
          
          
          case LOCO: {
            int n0 = bc[i++];
            int n1 = bc[i++];
            fn.aload(SC);
            for (int j = 0; j < n0; j++) fn.getfield(Scope.class, "parent", Scope.class);
            fn.getfield(Scope.class, "vars", Value[].class);
            fn.iconst(n1); fn.aaload();
            mstack = Math.max(mstack, cstack+2);
            cstack++;
            break;
          }
          case LOCM: {
            int n0 = bc[i++];
            int n1 = bc[i++];
            fn.new_(Local.class); fn.dup();
            fn.iconst(n0); fn.iconst(n1);
            fn.invspec(Local.class, "<init>", met(void.class, int.class, int.class));
            mstack = Math.max(mstack, cstack+4);
            cstack++;
            break;
          }
          
          
          case ARRO: {
            int n = bc[i++];
            fn.iconst(n); fn.anewarray(Value.class);
            for (int j = 0; j < n; j++) {
                                // .. v a
              fn.dup_x1();      // .. a v a
              fn.swap();        // .. a a v
              fn.iconst(n-j-1); // .. a a v n
              fn.swap();        // .. a a n v
              fn.aastore();     // .. a
            }
            fn.invstat(Arr.class, "create", met(Arr.class, Value[].class));
            
            mstack = Math.max(cstack+3, mstack);
            cstack-= n-1;
            break;
          }
          case ARRM: {
            int n = bc[i++];
            
            fn.iconst(n); fn.anewarray(Settable.class);
            for (int j = 0; j < n; j++) {
                                // .. v a
              fn.dup_x1();      // .. a v a
              fn.swap();        // .. a a v
              fn.iconst(n-j-1); // .. a a v n
              fn.swap();        // .. a a n v
              fn.aastore();     // .. a
            }
            fn.new_(SettableArr.class); //   a o
            fn.dup_x1();                // o a o
            fn.swap();                  // o o a
            fn.invspec(SettableArr.class, "<init>", met(void.class, Settable[].class));
            
            mstack = Math.max(cstack+3, mstack);
            cstack-= n-1;
            break;
          }
          case FN1C: {
            fn.swap();
            fn.invvirt(Value.class, "call", met(Value.class, Value.class));
            cstack--;
            break;
          }
          case FN2C: {      // x f w
            fn.astore(TMP); // x f    w
            fn.swap();      // f x    w
            fn.aload(TMP);  // f x w
            fn.swap();      // x f w
            fn.invvirt(Value.class, "call",  met(Value.class, Value.class, Value.class));
            cstack-= 2;
            break;
          }
          case FN1O: { Met.Lbl l1 = fn.lbl(), l2 = fn.lbl();
                       // x f
            fn.swap(); // f x
            fn.dup();  // f x x
            fn.is(Nothing.class); // f x B
            fn.ifeq0(l1); // if (x instanceof Nothing) {
              fn.swap(); // x f
              fn.pop();  // x   aka   ·
              fn.goto_(l2);
            l1.here();    // } else {
              fn.invvirt(Value.class, "call", met(Value.class, Value.class));
            l2.here();    // }
            mstack = Math.max(mstack, cstack+1);
            cstack--;
            break;
          }
          case FN2O: { Met.Lbl l1=fn.lbl(), l2=fn.lbl(), l3=fn.lbl();
            // x f w
            fn.astore(TMP); // x f
            fn.swap(); // f x
            fn.dup();  // f x x
            fn.is(Nothing.class); // f x B
            fn.ifeq0(l1); // if (x instanceof Nothing) {
              fn.swap(); fn.pop();
              fn.goto_(l3);
            l1.here();    // } else {
              fn.aload(TMP);
              fn.is(Nothing.class);
              fn.ifeq0(l2); // if (w instanceof Nothing) {
                fn.invvirt(Value.class, "call", met(Value.class, Value.class));
                fn.goto_(l3);
              l2.here();    // } else {
                fn.aload(TMP);
                fn.swap();
                fn.invvirt(Value.class, "call", met(Value.class, Value.class, Value.class));
                            // }
            l3.here();    // }
            mstack = Math.max(mstack, cstack+1);
            cstack-= 2;
            break;
          }
          case OP1D: {
            fn.swap(); fn.cast(Md1.class); fn.swap();
            
            fn.invvirt(Md1.class, "derive", met(Value.class, Value.class));
            cstack--;
            break;
          }
          case OP2D: {                //                ; g d f
            fn.astore(TMP);           // store f        ; g d
            fn.cast(Md2.class);       // cast d to 2-mod; g D
            fn.swap();                // place d below f; D g
            fn.aload(TMP); fn.swap(); // place g below f; D f g
            fn.invvirt(Md2.class, "derive", met(Value.class, Value.class, Value.class));
            cstack-= 2;
            break;
          }
          case OP2H: {
            // g d
            fn.cast(Md2.class);
            fn.swap();
            fn.invvirt(Md2.class, "derive", met(Md1.class, Value.class));
            
            cstack--;
            break;
          }
          case TR2D: {
            fn.astore(TMP); fn.astore(TMP2);
            fn.new_(Atop.class); fn.dup();
            fn.aload (TMP); fn.aload (TMP2);
            fn.invspec(Atop.class, "<init>", met(void.class, Value.class, Value.class));
            mstack = Math.max(mstack, cstack+2);
            cstack--;
            break;
          }
          case TR3D: {
            fn.astore(TMP); fn.astore(TMP2); fn.astore(TMP3);
            fn.new_(Fork.class); fn.dup();
            fn.aload (TMP); fn.aload (TMP2); fn.aload (TMP3);
            fn.invspec(Fork.class, "<init>", met(void.class, Value.class, Value.class, Value.class));
            mstack = Math.max(mstack, cstack+2);
            cstack-= 2;
            break;
          }
          case TR3O: { Met.Lbl l1 = fn.lbl(), l2 = fn.lbl();
            fn.astore(TMP); fn.astore(TMP2); fn.astore(TMP3);
            fn.aload(TMP);
            fn.is(Nothing.class);
            fn.ifeq0(l1); // if (f instanceof Nothing) {
              fn.new_(Atop.class); fn.dup();
              fn.aload(TMP2); fn.aload(TMP3); // g h
              fn.invspec(Atop.class, "<init>", met(void.class, Value.class, Value.class));
              fn.goto_(l2);
            l1.here();    // } else {
              fn.new_(Fork.class); fn.dup();
              fn.aload(TMP); fn.aload(TMP2); fn.aload(TMP3); // f g h
              fn.invspec(Fork.class, "<init>", met(void.class, Value.class, Value.class, Value.class));
            l2.here();    // }
            mstack = Math.max(mstack, cstack+2);
            cstack-= 2;
            break;
          }
          case SETU:
          case SETN: {
            fn.swap(); fn.dup_x1(); // v k → v k v
            fn.iconst(bc[pi]==SETU? 1 : 0); fn.aload(SC); fn.aconst_null();
            fn.invvirt(Settable.class, "set", met(void.class, Value.class, boolean.class, Scope.class, Callable.class));
            mstack = Math.max(mstack, cstack+4);
            cstack--;
            break;
          }
          case SETH: { Met.Lbl l = fn.lbl();
            fn.swap(); // v k → k v
            fn.aload(SC);
            fn.invvirt(Settable.class, "seth", met(boolean.class, Value.class, Scope.class));
            fn.ifne0(l); // if (!k.seth(v, sc)) {
              fn.aconst_null();
              fn.aret();
            l.here();    // }
            cstack-= 2;
            break;
          }
          case VFYM: {
            fn.new_(MatchSettable.class);
            fn.dup_x1(); fn.swap(); // s s o
            fn.invspec(MatchSettable.class, "<init>", met(void.class, Value.class));
            mstack = Math.max(mstack, cstack+2);
            break;
          }
          case SETM: {       // v f k
            fn.astore(TMP);  // v f
            fn.swap();       // f v
            fn.astore(TMP2); // f
            fn.aload(TMP);   // f k
            fn.dup_x1();     // k f k
            fn.aload(SC);    // k f k sc
            fn.invvirt(Settable.class, "get", met(Value.class, Scope.class)); // k f K
            fn.aload(TMP2);  // k f K v
            fn.invvirt(Value.class, "call",  met(Value.class, Value.class, Value.class)); // k n
            fn.dup_x1();     // n k n
            fn.iconst(1); fn.aload(SC); fn.aconst_null(); // n k n true sc null
            fn.invvirt(Settable.class, "set", met(void.class, Value.class, boolean.class, Scope.class, Callable.class)); // n
            mstack = Math.max(mstack, cstack+3);
            cstack-= 2;
            break;
          }
          case POPS: {
            fn.pop();
            cstack--;
            break;
          }
          case DFND: {
            int n = bc[i++];
            fn.aload(0);
            fn.getfield(JFn.class, "blocks", BlockTok[].class);
            fn.iconst(n);
            fn.aaload();
            fn.aload(SC);
            fn.invvirt(BlockTok.class, "eval", met(Value.class, Scope.class));
            mstack = Math.max(mstack, cstack+3);
            cstack++;
            break;
          }
          case FLDO: { Met.Lbl l = fn.lbl();
            int n = bc[i++];
            fn.dup(); fn.is(BQNObj.class); fn.ifne0(l); // if (!(ToS instanceof APLMap)) {
              fn.new_(DomainError.class); fn.dup();
              fn.ldc("Expected value to the left of '.' to be a namespace");
              fn.invspec(DomainError.class, "<init>", met(void.class, String.class));
              fn.athrow();
            l.here(); // }
            fn.cast(BQNObj.class);
            fn.ldc(comp.objs[n].asString());
            fn.invvirt(BQNObj.class, "getChk", met(Value.class, String.class));
            mstack = Math.max(mstack, cstack+3);
            break;
          }
          case FLDM: { Met.Lbl l = fn.lbl();
            int n = bc[i++];
            fn.dup(); fn.is(BQNObj.class); fn.ifne0(l); // if (!(ToS instanceof APLMap)) {
              fn.new_(DomainError.class); fn.dup();
              fn.ldc("Expected value to the left of '.' to be a namespace");
              fn.invspec(DomainError.class, "<init>", met(void.class, String.class));
              fn.athrow();
            l.here(); // }
            fn.cast(BQNObj.class);
            fn.ldc(comp.objs[n].asString());
            fn.invvirt(BQNObj.class, "getMut", met(BQNObj.MapPointer.class, String.class));
            mstack = Math.max(mstack, cstack+3);
            break;
          }
          case NSPM: {
            int n0 = bc[i++];
            int n1 = bc[i++];
            fn.iconst(n0); fn.anewarray(Settable.class);
            for (int j = 0; j < n0; j++) {
                                 // .. v a
              fn.dup_x1();       // .. a v a
              fn.swap();         // .. a a v
              fn.iconst(n0-j-1); // .. a a v n
              fn.swap();         // .. a a n v
              fn.aastore();      // .. a
            }
            fn.new_(SettableNS.class); //   a o
            fn.dup_x1();               // o a o
            fn.swap();                 // o o a
            fn.aload(0);
            fn.getfield(JFn.class, "vals", Value[].class);
            fn.iconst(n1);
            fn.aaload();
            fn.invspec(SettableNS.class, "<init>", met(void.class, Settable[].class, Value.class));
            mstack = Math.max(cstack+4, mstack);
            cstack-= n0-1;
            break;
          }
          case CHKV: { Met.Lbl l = fn.lbl();
            fn.dup();
            fn.is(Nothing.class);
            fn.ifeq0(l);
              fn.pop();
              fn.new_(ValueError.class); fn.dup();
              fn.ldc("Didn't expect · here");
              fn.invspec(ValueError.class, "<init>", met(void.class, String.class));
              fn.athrow();
            l.here();
            mstack = Math.max(mstack, cstack+3);
            break;
          }
          case RETN: {
            break loop;
          }
          case RETD: {
            fn.new_(Namespace.class); fn.dup();
            fn.aload(SC);
            fn.aload(BODY);
            fn.getfield(Body.class, "exp", HashMap.class);
            fn.invspec(Namespace.class, "<init>", met(void.class, Scope.class, HashMap.class));
            mstack = Math.max(mstack, cstack+4);
            cstack++;
            break loop;
          }
          case SPEC: {
            switch(bc[i++]) {
              case EVAL:
                fn.new_(EvalBuiltin.class); fn.dup();
                fn.aload(SC);
                fn.invspec(EvalBuiltin.class, "<init>", met(void.class, Scope.class));
                break;
              case STDOUT:
                fn.new_(Quad.class); fn.dup();
                fn.invspec(Quad.class, "<init>", met(void.class));
                break;
              case STDIN:
                fn.new_(Quad.class); fn.dup();
                fn.invspec(Quad.class, "<init>", met(void.class));
                fn.aload(SC);
                fn.invvirt(Quad.class, "get", met(Value.class, Scope.class));
                break;
              default:
                throw new InternalError("Unknown special "+bc[i-1]);
            }
            mstack = Math.max(mstack, cstack+3);
            cstack++;
            break;
          }
        }
      }
      if (cstack!=1) throw new ImplementationError("stack size at end was "+cstack); // see RETN
      fn.mstack = mstack;
      fn.aret();
      methods.add(fn);
    }
    
    
    String name = "gen" + ctr++;
    int this_class = CONSTANT_Class("BQN/"+name);
    int super_class = CONSTANT_Class(JFn.class);
    byte[] bc = finish(this_class, super_class);
    if (bc==null) {
      r = null;
      return;
    }
    Class<?> def = l.def(null, bc, 0, bc.length);
    try {
      JFn o = (JFn) def.getDeclaredConstructor().newInstance();
      o.vals = comp.objs;
      o.blocks = comp.blocks;
      // Method get = o.getClass().getDeclaredMethod("get", Scope.class);
      // System.out.println(get.invoke(o, new Object[]{null}));
      r = o;
    } catch (Throwable e) {
      throw new ImplementationError(e);
    }
  }
}
