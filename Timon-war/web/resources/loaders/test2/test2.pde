
PFont helvetica12;

Voter v;

void setup() {
  size(970, 300);
  v = new Voter();
  background(190, 190, 190, 50);
  helvetica12 = createFont("Helvetica", 12);
  textFont(helvetica12);
  smooth();
  stroke(0);
}

void draw() {
  background(200);
  pushMatrix();
  translate(width/2, height/2);  
  v.display();
  rotate(v.rot);  
  popMatrix();
  text("rot="+v.rot,10,20);
  
}


class Voter {
  int n;
  float r;
  Punto[] p;
  int rot=0;
  Voter() {
    n = int(random(5, 20));
    r = 80;
    p = new Punto[n];
    float th=0;
    for (int i=0; i<n; i++) {    
      float x = (r * cos(th));
      float y = (r * sin(th));
      th += (TWO_PI)/n;
      p[i] = new Punto(x, y);
    }
  }
  void display() {

    pushMatrix();
    for (int i=0; i<n; i++) {
      for (int j=0; j<n; j++) {
        stroke(0);
        line(p[i].x, p[i].y, p[j].x, p[j].y);
      }
    }
    stroke(0);
    fill(255); 
    for (int i=0; i<n; i++) {   
      ellipse(p[i].x, p[i].y, 20, 20);
    }
    rotate(rot);
    popMatrix();
    
    rot++;
  }
}


class Punto {
  float x;
  float y;
  Punto(float x, float y) {
    this.x = x;
    this.y = y;
  }
}


void mouseClicked() {
  setup();

}
