# PYSTM  
class PYSTM is the code of PYSTM topic model  
## Input are 3 texts  
### First is the text about short text, one line is a text  
example：  
0 1 2 3 4 5 6 7 8 9 10 11 5   
12 13 14 15 16 17 18 19 20 21 22 23 24 25   
26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45   
words of texts are changed to id  
  
### Second is the text about dictionary, first line is the length of dictionary, from the second line are words, one line is a word 
example：  
19670  
decision  
means  
ruling  
months  
regular  
word id is line number - 1  
  
### Third is the text about labels, one line is a label   
expame:  
sport  
business  
sport  
entertainment  
us  
entertainment  
world  
line number is text id  
  
## Output are 2 texts    
### First is the text about topics and Top-N words    
example:  
year state government president week people years states united billion   
kentucky derby winner champion stakes life animal title black star   
open french final nadal world title djokovic win novak ivory   
league players coach team nfl baseball basketball tournament season player  
  
### Second is the text about topics and probability with short texts, one line is a text    
example:
0 0:0.7840236686390533 1:4.2265426880811494E-4 2:0.002113271344040575 3:0.12299239222316145 4:0.0 5:4.2265426880811494E-4 6:0.002113271344040575 7:0.006762468300929839 8:0.0 9:0.0038038884192730348 10:4.2265426880811494E-4 11:0.006762468300929839 12:0.002113271344040575 13:0.06804733727810651 14:0.0   
1 0:0.8765849535080305 1:0.03169907016060862 2:4.2265426880811494E-4 3:4.2265426880811494E-4 4:0.0016906170752324597 5:8.453085376162299E-4 6:8.453085376162299E-4 7:0.019019442096365174 8:0.0038038884192730348 9:4.2265426880811494E-4 10:0.002113271344040575 11:0.0038038884192730348 12:0.057480980557903634 13:4.2265426880811494E-4 14:4.2265426880811494E-4   
0 0:0.47368421052631576 1:0.018796992481203006 2:5.78368999421631E-4 3:0.2183342972816657 4:0.001156737998843262 5:0.006940427993059572 6:2.891844997108155E-4 7:0.008675534991324466 8:0.001156737998843262 9:0.025448235974551765 10:0.045980335454019666 11:0.014748409485251591 12:0.16946211683053788 13:0.00578368999421631 14:0.008964719491035281   

