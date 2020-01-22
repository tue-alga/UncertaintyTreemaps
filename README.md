This is the source code used for the paper: "Uncertainty Treemaps". It builds upon the source code from https://gitaga.win.tue.nl/max/IncrementalTreemap which contains a large amount of treemaping algorithms and allows for visualisation. The most significant changes made are the inclusion of mask-friendly and mask-aware algorithms as described in the paper, and allowing uncertainty alongside the "real" values of the data. 

It can be started using the main method in "Visualiser.java". This will start up a gui where one can view the results of the algorithms on the different datasets.



Algorithms implemented:

Approximation (+Mask-friendly +Mask-aware): An approximation algorithm for dissecting a rectangle into rectangles with specified areas. Discrete Applied Mathematics, 155(4):523–537, 2007

Hilbert: 							S. Tak and A. Cockburn. Enhanced spatial stability with Hilbert and Moore treemaps. IEEE Transactions on Visualization and Computer Graphics,19(1):141–148, 2013.

Local Moves: 						M. Sondag, B. Speckmann, and K. Verbeek. Stable treemaps via local moves. IEEE Transactions on Visualization and Computer Graphics, 24(1):729–738, Jan 2018.

Moore: 								S. Tak and A. Cockburn. Enhanced spatial stability with Hilbert and Moore treemaps. IEEE Transactions on Visualization and Computer Graphics,19(1):141–148, 2013.

Pivot-by-(Middle,Split,Split-Size): B. Shneiderman and M. Wattenberg. Ordered treemap layouts. In Proc. IEEE Symp. on Information Visualization (InfoVis), pp. 73–78, 2001.

Slice and Dice: 					B. Shneiderman. Tree visualization with tree-maps: a 2D space-filling approach. ACM Transactions on Graphics, 11(1):92–99, 1992

Strip(+Mask-friendly): 								B. B. Bederson, B. Shneiderman, and M. Wattenberg. Ordered and quantum treemaps: Making effective use of 2D space to display hierarchies. ACM Trans. Graph., 21(4):833–854, Oct. 2002.

Spiral (+Mask-friendly): 							Y. Tu and H.-W. Shen. Visualizing changes of hierarchical data using treemaps. IEEE TVCG, 13(6):1286–1293, 2007.

Split (+Mask-friendly): 								B. Engdahl. Ordered and unordered treemap algorithms and their applications on handheld devices, 2005. MSc thesis, Tech. Report TRITA-NA-E05033, Dept. of Comp. Sci., Stockholm Royal Institute of Technology,Sweden.

Squarified (+Mask-friendly):  						Perceptual guidelines for creating rectangular treemaps. IEEE Transactions on Visualization and Computer Graphics, 16(6):990–998, 2010.





Original sources for the datasets included in this repository:

US Bureau of Labor Statistics. Mean expenditure per (consumer) household in 2014. https://stats.bls.gov/cex/programs/r14.zip
Standard & Poor's 500: Mean closing price per stock over the period 03-11-2016 to 10-11-2016.
UN comtrade database. Coffee imported per country over the period 1994-2014. https://comtrade.un.org. 
Worldbank. Infant deaths per country over the period 1992-2016. https://data.worldbank.org/indicator/SH.DTH.IMRT.
