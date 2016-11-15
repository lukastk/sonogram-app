
# coding: utf-8

import numpy as np
import math

f = open("data/leberExport.vol1.vol","r") # Öffne die Datei
f.read(291) # Überspringe die ersten 291 Bytes

data       = f.read(2) # Schlüsselwort / Tag
tag        = np.frombuffer(data, np.uint8)[0] # interpretiere als unsigned short (2 Byte)

print type(data)
print int(tag)

data       = f.read(4) # Größe des Datensatzes
tag_length = np.frombuffer(data, np.uint32)[0] # interpretiere als unsigned long (4 Byte)

print "Sind wir an der richtigen Stelle?", tag == int(0x0050) # Tag: 80 = int(0x0050)
print "Die Größe des Datensatzes in Bytes:", tag_length

data   = f.read(tag_length) # 3D Datensatz auslesen
# Z Y X

VOLUME = np.frombuffer(data, np.uint8).reshape(616,330,410)#interpretiere als unsigned short

data_size_x = 410.0
data_size_y = 330.0
data_size_z = 616.0

cf = 2

new_x = int(data_size_x / cf)
new_y = int(data_size_y / cf)
new_z = int(data_size_z / cf)

data = []

data

for z in range(new_z):
    for y in range(new_y):
        for x in range(new_x):

            ox = x*cf
            oy = y*cf
            oz = z*cf

            d = 0
            val = 0

            #print ox, oy, oz

            if ox > 0:
                val += VOLUME[oz, oy, ox - 1]
                d += 1
            if ox < data_size_y - 1:
                val += VOLUME[oz, oy, ox + 1]
                d += 1
            if oy > 0:
                val += VOLUME[oz, oy - 1, ox]
                d += 1
            if oy < data_size_y - 1:
                val += VOLUME[oz, oy + 1, ox]
                d += 1
            if oz > 0:
                val += VOLUME[oz - 1, oy, ox]
                d += 1
            if oz < data_size_y - 1:
                val += VOLUME[oz + 1, oy, ox]
                d += 1

            val /= (2*d)

            data.append(val)

f = open("out.vol", "wb")

f.write(outd)
f.close()