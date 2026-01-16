# Graphics-Engine
Simple 3D Engine (Java)

This project is a small 3D graphics engine written in plain Java. It draws 3D shapes in a 2D window using basic math — no external libraries.

What it Does

Draws 3D wireframe objects (like cubes)

Rotates them in 3D space

Uses perspective so things look smaller when they're farther away

Perspective Projection (Quick Explanation)

Perspective projection makes 3D stuff look like it has depth. The idea is simple:
the farther something is (bigger z), the smaller it shows up on screen.

Basic idea in math form:

𝑥
′
=
𝑥
𝑧
,
𝑦
′
=
𝑦
𝑧
x
′
=
z
x
	​

,y
′
=
z
y
	​


So if z is large → x'/y' get smaller → looks farther away.

Rotation Matrices (Quick Explanation)

Rotation matrices are small math grids that rotate points around an axis.

For example, rotating around the X axis uses:

[
1
	
0
	
0


0
	
cos
⁡
𝜃
	
−
sin
⁡
𝜃


0
	
sin
⁡
𝜃
	
cos
⁡
𝜃
]
	​

1
0
0
	​

0
cosθ
sinθ
	​

0
−sinθ
cosθ
	​

	​


There are very similar ones for Y and Z. These let the engine spin objects smoothly.

How to Run

Open the project in your Java IDE

Run the main file

Watch the shape spin!

Future Stuff (Maybe)

Cameras

Filled polygons

Basic lighting

Better controls
