
class Crowd
{
  int crowdSize;
  ArrayList<Boid> list;
  
  Crowd(int crowdSize)
  {
    this.crowdSize = crowdSize;
    list = new ArrayList<Boid>(crowdSize);
    for(int i=0; i<crowdSize; i++)
    {
      list.add(new Boid(width/2,height/2));
    }
  }
  
  void run()
  {
    for(int i=0; i<list.size(); i++)
    {
      list.get(i).update();
      list.get(i).display();
    }
  }
  
  void addNew(float x, float y)
  {
    list.add(new Boid(x,y));
  }
  
  //----
  
  void update()
  {
    for(int i=0; i<list.size(); i++)
    {
      list.get(i).update();
    }
  }
  
  void display()
  {
    for(int i=0; i<list.size(); i++)
    {
      list.get(i).display();
    }
  }
  
}