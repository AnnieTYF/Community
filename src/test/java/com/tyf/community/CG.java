package com.tyf.community;

class CB {
    private static int loan;

    public CB(){
        System.out.println("1.0" + this.loan);
    }

    public CB(int loan){
        this.loan = loan;
        System.out.println("2.0"+ ++this.loan);
    }

    static {
        System.out.println("3.0" + loan + 1);
    }
}
class CCC extends CB{
    private static int loan;
    public CCC(){
        System.out.println("4.0" + ++this.loan);
    }
    public CCC(int loan){
        this.loan = loan;
        System.out.println("5.0" + this.loan++);
    }
    static {
        System.out.println("6.0" + loan + 1);
    }
}
public class CG {
    public static void main(String[] args){
        new CCC(0b101);
    }
}
