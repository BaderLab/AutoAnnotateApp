.. _label_options:

Label Options
=============

AutoAnnotate uses the WordCould app to generate cluster labels. Some basic label
options are available from the AutoAnnotate panel menu, but more advanced options
are available through WordCloud directly.


WordCloud App
-------------

The WordCloud app generates a "word cloud" for selected nodes based on one or more attributes (columns)
of choice.

.. image:: images/word_cloud.png
   :width: 550 px

AutoAnnotate invokes WordCloud to generate a word tag cloud for the node data within each cluster, 
which is used to derive the text for the label annotations.


Basic Label Options
-------------------

Basic label options are available from the AutoAnnotate panel menu at **Settings > Label Options...**.

.. image:: images/menu_label_options.png
   :width: 550 px

This will open the **Label Options** dialog. (These are the same options available in the **Create
Annotation Set Dialog**.)

There are two label algorithms available...

Biggest Words
~~~~~~~~~~~~~

.. image:: images/label_options_dialog_biggest.png
   :width: 400 px

The “Biggest Words” option generates a word cloud for the nodes in each cluster.
The words are sorted by size then the N largest words are chosen.

The only option available is to choose the maximum number of words in each label.

Adjacent Words
~~~~~~~~~~~~~~

.. image:: images/label_options_dialog_adjacent.png
   :width: 400 px

The “Adjacent Words” option is a heuristic that attempts to balance word size with 
word adjacency. Words that are adjacent to the largest words are given a size bonus
making them more likely to be chosen for the label. 

A word cloud is generated for the nodes in each cluster.
The words are sorted by size, then a size bonus is added 
to every word that is adjacennt to the N largest words. This causes 
words that are adjacent to the N largest words to be more likely to be chosen. 
Then the list is sorted again by size and the N largest words (with bonus) are selected.

The **adjacent word bonus** option is used to decide how much of a size bonus to 
give to words that are adjacent to the largest workds. The default value is 8.


WordCloud Options
-----------------

In the main menu select **Apps > WordCloud > Show WordCloud**.

In the WordCloud panel you can change options that effect the contents of each cluster's word cloud,
for example using the Normalize slider.

Back in the AutoAnnotate panel select **Recalculate Labels** to regenerate labels using the updated 
WordCloud options. 


Removing Words from Labels
--------------------------

If you do not want certain words to appear in labels they can be removed using WordCloud.

* Expand the **Advanced Options** section in the WordCloud panel.
* Click the **Excluded Words** button.
* A dialog will show.
* Enter the word that should be excluded, then click **Add**.
* Click **Ok**.
* Go back to the AutoAnnotate panel and select **Recalculate Labels** from the menu. The labels will
  be regenerated and the excluded words will no longer be chosen.



