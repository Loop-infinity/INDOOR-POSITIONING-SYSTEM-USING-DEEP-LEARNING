package dspanah.sensor_based_har;

public class Node {

    public float x;
    public float y;
    int neighbours[];
  //  public boolean deadEnd;

    public Node(float x,float y,int ...neighbours)
    {
        this.x = x;
        this.y = y;
        System.out.println("700x :"+x+" 700y :"+y+"\t 1060x: "+((int)(x * 1.51f))+" 1060y: "+(int)(y * 1.51f));
        this.neighbours = neighbours;
    }
}
