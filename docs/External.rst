Using an External Clustering Algorithm
--------------------------------------

AutoAnnotate allows clusters to be defined using any node attribute via the **User-defined clusters**
option on the Advanced panel. This allows clustering algorithms provided by other Cytoscape
Apps or by external scripts to be used with AutoAnnotate.


Using clusterMaker Manually
~~~~~~~~~~~~~~~~~~~~~~~~~~~

You may use some clusterMaker algorithms not provided through the **New Annotation Set** dialog.

Here is an example using the clusterMaker Transitivity Clustering algorithm. 

* Select **Apps > clusterMaker Cluster Network > Transitivity Clustering**. 
* A dialog with algorithm settings will be shown. 
* Expand the **Cytoscape Advanced Settings** section and take note of the **Cluster Attribute Name**, in this case it is **__transclustCluster**. 
* Click **Ok** to run the algorithm.
* A node attribute (column) called **__transclustCluster** is created where each node is assigned a cluster identifier.
* Open the **New Annotation Set** dialog and choose the Advanced panel. 
* Select **User-defined clusters** and then select the **__transclustCluster** column. 
* Click **Create Annotations**.

AutoAnnotate does not support overlapping clusters. Some clusterMaker algorithms create 
list columns that may assign some nodes to more than one cluster. These columns cannot 
be used with AutoAnnotate.



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
