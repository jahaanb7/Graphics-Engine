# Simple 3D Engine from scratch in Java

This project is a small 3D graphics engine written in plain Java. It draws simple 3D objects onto a 2D screen using basic math and no external libraries.

## What It Does
- Draws 3D wireframe objects (like cubes)
- Rotates them in 3D space
- Uses perspective projection to simulate depth

## Perspective Projection (Quick Explanation)
Perspective projection makes 3D objects appear smaller as they move further away from the viewer.

Basic idea:
x' = x / z
y' = y / z

If `z` is large → `x'` and `y'` get smaller → object looks farther away.

## Rotation Matrices (Quick Explanation)
Rotation matrices rotate points around an axis in 3D.

Example (rotation around the X axis):
[ 1       0        0      ]
[ 0   cosθ   -sinθ ]
[ 0   sinθ    cosθ ]

Similar matrices exist for rotation around Y and Z.

## How to Run
1. Open the project in your Java IDE
2. Run the main file
3. A spinning 3D shape should appear

## Future Ideas
- Camera system
- Filled polygons / hidden surface removal
- Simple lighting
- User controls for movement/rotation
