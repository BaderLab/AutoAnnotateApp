Using an External Clustering Algorithm
--------------------------------------

AutoAnnotate allows clusters to be defined using any node attribute via the **User-defined clusters**
option on the Advanced panel. This allows clustering algorithms provided by other Cytoscape
Apps or by external scripts to be used with AutoAnnotate.


Using clusterMaker Manually
~~~~~~~~~~~~~~~~~~~~~~~~~~~

You may use some clusterMaker algorithm not provided through the **New Annotation Set** dialog.

Here is an example using the clusterMaker Transitivity Clustering algorithm. 

* Select **Apps > clusterMaker Cluster Network > Transitivity Clustering**. 
* A dialog with algorithm settings will be shown. 
* Expand the **Cytoscape Advanced Settings** section and make note of the **Cluster Attribute Name**, in this case it is **__transclustCluster**. 
* Click **Ok** to run the algorithm.
* A node attribute (column) called **__transclustCluster** is created where each node is assigned a cluster identifier.
* Open the **New Annotation Set** dialog and choose the Advanced panel. 
* Select **User-defined clusters** and then select the **__transclustCluster** column. 
* Click **Create Annotations**.

AutoAnnotate does not support overlapping clusters. Some clusterMaker algorithms create 
list columns that may assign some nodes to more than one cluster. These columns cannot 
be used with AutoAnnotate.


Using the MCODE App
~~~~~~~~~~~~~~~~~~~

The MCODE App is a popular clustering App that works well with AutoAnnotate.

* Install MCODE: https://apps.cytoscape.org/apps/mcode

Using MCODE with AutoAnnotate

* Select **Apps > MCODE**
* A dialog with algorithm settings will be shown. 

  * If the dialog does not appear, then go to the MCODE panel and click the (+) icon at the top of the panel.

* In the MCODE dialog click the **Analyze Current Network** button.
* Open the **New Annotation Set** dialog and choose the Advanced panel. 
* Select **User-defined clusters** and then select the MCODE **Clusters (1)** column. 
* Click **Create Annotations**.


Using External Scripts
~~~~~~~~~~~~~~~~~~~~~~

See the :ref:`automating` for details on how to automate Cytoscape using external scripts.

A script must do the following to provide clusters to AutoAnnotate:

* Create a node attribute (column), any type may be used.
* Use the node attribute to assign a cluster identifier to each node. The cluster identifier may be blank, 
  those nodes will not be included in clusters.
* Open the **New Annotation Set** dialog and choose the Advanced panel. 
* Select **User-defined clusters** and then select the **__mcodeCluster** column. 
* Click **Create Annotations**.
