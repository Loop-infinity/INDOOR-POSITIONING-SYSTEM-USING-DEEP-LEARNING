package dspanah.sensor_based_har;

public class Node {

    public int x;
    public int y;
    int neighbours[];
  //  public boolean deadEnd;

    public Node(int x,int y,int ...neighbours)
    {
        this.x = x;
        this.y = y;
        this.neighbours = neighbours;
    }
}
