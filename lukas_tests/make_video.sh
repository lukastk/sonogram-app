avconv -r 10 -start_number 1 -i output/slice%d.png -b:v 1000k slices.mp4
