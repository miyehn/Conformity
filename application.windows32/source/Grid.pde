class Grid
{
  int gridSize;
  int bleed;
  int canvasWidth;
  int canvasHeight;
  int r;
  int c;
  ArrayList<Boid>[][] object;//a 2d array of arraylists
  
  PVector toMove;
  
  Grid (int gridSize)
  {
    this.gridSize = gridSize;
    this.bleed = gridSize*2;
    this.canvasWidth = width+bleed*2;
    this.canvasHeight = height+bleed*2;
    this.r = canvasHeight/gridSize+1;
    this.c = canvasWidth/gridSize+1;
    object = new ArrayList[r][c];
    for(int i=0; i<r; i++)
    {
      for(int j=0; j<c; j++)
      {
        object[i][j] = new ArrayList<Boid>();
      }
    }
    this.toMove = new PVector(0,0);
  }
  
  void removeElem(Boid B)
  {
    object[B.gridR][B.gridC].remove(B);
  }
  
  void encodeElem(Boid B)
  {
    int elemR = (((int)B.coord.y + bleed) / gridSize) % r;
    int elemC = (((int)B.coord.x + bleed) / gridSize) % c;
    B.gridR = elemR; B.gridC = elemC;
    object[elemR][elemC].add(B);
  }
  
  ArrayList<Boid> returnElemList(int r, int c)
  {
    return object[r][c];
  }
  
  //----
  
  void calcToCenter(PVector vec)
  {
    this.toMove = vec.mult(0.05);//arbitrary variable / camera moving speed
  }
  
}