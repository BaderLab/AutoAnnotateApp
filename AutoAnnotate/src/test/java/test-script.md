AutoAnnotate - Manual Test Script
=================================

- This script was written for AutoAnnotate 1.0 and Cytoscape 3.4.
- Any defects found should be entered into the AutoAnnotate github issue tracker.

# Setup

- Open session `Mesen_vs_Immuno_rnaseq_EM_annot_aa1.cys`
- Select all the nodes in the network view then click the 'New Network From Selection' toolbar button.
- Rename the network views to `Net1` and `Net2`

#### TODO
 - Test multiple network views per network with Cytoscape 3.4
 
 
# Part 1) Create Annotation Set Dialog (Basic)

#### Step 1 - Create Annotation Set with Defaults

- Select Net1
- Open the Create Annotation Set Dialog
- **Verify** The defaults should be 
 - Use clusterMaker is selected
 - Cluster Algorithm: `MCL Cluster`
 - Edge weight column: `EM1_Similarity_coefficient`
 - Label column: `EM1_GS_DESCR`
 - Label algorithm: `WordCloud: Adjacent Words`
 - Max words: 4, Adjacent word bonus: 8
- Click `Create Annotations`
- **Verify**: 
 - A new annotation set is created. 
 - The clusters should be drawn, they should mostly correspond to the original layout of the network.
 - The AutoAnnotate panel should be visible on the left. 
 - The AutoAnnotate Display panel should be visible on the right.
 - There should be a list of clusters in the cluster table.
 
#### Step 2 - Create Annotation Set from Cluster IDs
- Select Net1
- Open the Create Annotation Set Dialog
- Select `User-defined clusters`
- For cluster node ID column select `EM2_Coloring_dataset1`
- Click `Create Annotations`
- **Verify**
 - A new annotation set is created
 - It has two clusters
  - one the surrounds all the blue nodes (207 nodes)
  - the other surrounds all the red nodes (89 nodes)
 
#### Step 3 - Create another Annotation Set
- Select Net2
- Open the Create Annotation Set Dialog
- Select `SPCS Cluster`
- For Label Algorithm select `WordCloud: Biggest Words`
- For max words select 6
- Click Create Annotations
- **Verify** The annotation set is created


# Part 2) Using the Cluster Panel and Network View

#### Step 1 - Annotation Set Dropdown
- Select Net1
- **Verify** The annotation set dropdown (at the top of the cluster panel) has three items: (None), MCL Cluster..., and EM1_Coloring...
- Select 'MCL Cluster...'
 - **Verify** The annotations are drawn properly
- Select 'EM1_coloring...'
 - **Verify** The annotations are drawn properly
- Select '(None)'
 - **Verify** The annotations are all erased. No annotations are drawn.
- Select the Net2 network view
 - **Verify** The annotation set dropdown has two items: (None), SPCS Cluster...

#### Step 2 - Cluster Table
- Select Net1, and select the MCL Cluster... annotation set.
- Click the "Cluster" header in the table
 - **Verify** The table is sorted by cluster label
- Click the "Nodes" header
 - **Verify** The table is sorted by number of nodes
- Click on the row with the largest cluster (should be 109 nodes)
 - **Verify**
 - In the network view only the nodes that belong to the cluster are selected. (The Network Panel should show 109 selected nodes.)
 - The annotations should appear selected. The cluster label and the ellipse should be yellow.
- Hold Command (or ctrl on windows) and click on the next largest cluster in the table.
 - **Verify**
 - Both clusters are selected.
 - In the network view only the nodes that belong to either of the two clusters are selected.
- Click on the background of the network view.
 - **Verify** Nothing is selected anymore.
- In the network view draw a selection box around a single cluster.
 - **Verify** That cluster becomes selected in the cluster table.
 - The annotations should appear selected (become yellow).
 
#### Step 3 - Manually create cluster
- Near the bottom of the network there should be a small group of 4 red nodes that are not part of a cluster. (Probably because they are not connected by edges.)
- Select these 4 nodes.
- Right Click in the network view. Select Apps > AutoAnnotate > Create Cluster
- **Verify** The cluster is created
 - There should now be an ellipse annotation drawn around the nodes and a label drawn above them.
 - The cluster should be selected in the cluster table.

#### Step 4 - Delete a node from a cluster
- Select one of the nodes in the cluster you just created in Step 3.
- Delete the node (Edit > Cut)
- **Verify**
 - The cluster now contains 3 nodes.
 - It says 3 in the nodes column for that cluster.
- Select the remaining 3 nodes in the cluster
- Delete them
- **Verify**
 - The cluster is removed from the cluster table.
 
#### Step 5 - Move nodes
- Select some nodes in the network view and move them around.
- **Verify**
 - The annotations are redrawn in real time as you move the nodes.
 - The ellipse annotations are always surrounding the nodes of the cluster.
 - Play around with this for a bit. Make sure it always words.


# Part 3) The Display Options Panel

#### Step 1 - Test all controls
- Make sure Net1 and MCL CLuster... are selected.
- Move the `Border Width` slider back and forth.
 - **Verify** The borders of all the annotations get larger/smaller as the slider is moved.
- Move the `Opacity` slider back and forth
 - **Verify** The opacity of the shape annotations changes as the slider is moved.
 - If the slider is all the way to the left (0%) then only the border is visible.
- Move the `Font Scale` slider back and forth
 - **Verify** The size of the text annotations changes as the slider is moved.
- Deselect 'scale font by cluster size'
 - **Verify**
 - All of the text annotations in the network view are now the same font size
 - The slider now says 'Font Size' (and should default to 20)
- Move the `Font Size` slider back and forth
 - **Verify** The font size of all the text annotations changes
- Select 'Scale font by cluster size' again
- Under shape click Rectangle
 - **Verify** all the shape annotations are now Rectangles
- Click Ellipse
 - **Verify** all the shapes are now ellipses again
- Click Hide Clusters
 - **Verify** All the shape annotations disappear
- Click Hide Labels
 - **Verify** ALl the text annotations disappear
- Un-click Hide clusters and Hide labels
 - **Verify** All the annotations are visible again
 
 
 
# Part 4) Cluster Menu

#### Step 1 - Delete
- Make sure Net1 and MCL CLuster... are selected.
- Select one of the smallest clusters (2 nodes)
- Right click it, select Delete
 - **Verify**
 - The annotations are deleted from the network.
 - The cluster is deleted from the cluster table.
 - The actual nodes from the cluster **are not deleted**.
 
#### Step 2 - Rename
- Select one of the clusters
- Right click > Rename
- Enter a new name
- **Verify**
 - The cluster label changes to the name you entered
 - The text annotation in the network view shows the label you entered
 
#### Step 3 - Merge
- In the network view select two clusters that are close to each other.
- Right click on one of the clusters in the table.
- Select Merge
- **Verify**
 - The two cluters are merged into one.
 - There should now be one ellipse surrounding all the nodes.
 - A new label is automatically generated.
 
#### Step 4 - Extract
- In the cluster table select all the clusters that have 2 nodes.
- Right click, select Extract
- In the pop-up enter "Extracted Clusters" for the new annotation set name.
- **Verify**
 - A new Annotation Set consisting of just the clusters with 2 nodes is created.
- In the annotation set dropdown switch back to the MCL Cluster annotation set.

#### Step 5 - Collapse/Expand
- Select the 2 largest clusters (the ones with 109 and 29 nodes)
- Right click, select Collapse
- **Verify**
 - The two clusters are collapsed (and meta-edges are created).
 - The two clusters now have checkmarks in the 'Collapsed' column.
- Select one of the collapsed clusters in the network view.
 - **Verify** The cluster becomes selected in the cluster table
- Select both of the clusters in the cluster table
- Right click, select Collapse
 - **Verify**  nothing happens, the clusters were already collapsed
- Select both clusters again
- Right Click, select Expand
 - **Verify** The clusters are expanded properly. Everything goes back to how it was before they were collapsed.
 
#### Step 6 - Recalculate Labels
- Go to the Annotation Set menu
- Select Label Options...
- Set the max words to 1
- Go to the cluster table and select all the small clusters of size 2
- Right click, select Recalculate Labels
- **Verify**
 - All the new labels have exactly 1 word
- Go back to the Label Options panel and reset the max words to what it was before you changed it.


# Part 5 - Annotation Set Menu

#### Step 1 - New Annotation Set
- Select "New Annotation Set..." from the annotation set menu (at the top of the cluster panel).
 - **Verify** The Create Annotation Set dialog appears.
- Click "Create Annotation Set"
 - **Verify** A new annotation set is created
 - The new annotation set is selected in the dropdown
 
#### Step 2 - Rename
- Select Rename
- Enter a new name
- **Verify**
 - The annotation set dropdown shows the new name
 
#### Step 3 - Delete
- Select "Delete"
 - **Verify** 
 - The annotation set you created in step 1 is deleted.
 - The annotation set dropdown says (none)

#### Step 4 - Collapse All/Expand All
- Make sure the 'MCL Cluster...' annotation set is selected.
- Select "Collapse All"
 - **Verify** All the clusters are collapsed
 - All of the clusters have a checkmark in the collapsed column.
- Select "Expand All"
 - **Verify** All the clusters go back to normal
- Select "Collapse All"
- In the dropdown select "(none)"
 - **Verify** All the clusters are automatically expanded before switching to (none)
- Switch back to 'MCL Cluster..."
 - **Verify**
 - All the cluster annotations are drawn correctly
 - None of the clusters are collapsed.
 
#### Step 5 - Layout Clusters
- Select Layout Clusters
 - **Verify** That the layout runs without an error.

#### Step 6 - Redraw Annotations
- Select Redraw Annotations
 - **Verify** All the annotations are quickly redrawn.
 - The annotations in the Net2 network view are untouched (important).

#### Step 7 - Labels
- Select Label Options
- Set max words to 1
- Select Recalculate Labels
 - **Verify** All the labels have exactly one word


# Part 6 - Session Save/Load

#### Step 1 - Review
- At this point you should have 2 network views, Net1 and Net2.
- Net1 should have 3 annotation sets, MCL Cluster, Extracted Clusters, and the annotation set created from EM1_Coloring...
- Net2 should have 1 annotation set SPCS Clusters
- Verify that the above is correct. If not you can repeat Part 1 and Part 4 Step 4.
- You may want to take some screenshots of the annotation sets to verify that they restore properly.

#### Step 2 - Save and Restore
- Go to File > Save As...
- Save the session under a new file name.
- Restart Cytoscape
- Load the session file you just saved
- **Verify**
 - Make sure that all the annotation sets are restored properly.
 
#### Step 3 - Label Options
- Select Net1 and MCL Cluster...
- Go to Label Options
- Select WordCloud: Biggest Labels
- Select max words: 1
- Save the session
- Restart cytoscape
- Load the session
- Go to Label Options (for MCL Cluster...)
- **Verify** That WordCloud: Biggest Words and max words: 1 are selected.

#### Step 4 - Expand before Save
- Select Net1 and MCL Cluster...
- Select "Collapse All"
- Save the session
 - **Verify** That all the nodes are expanded before the save
- Restart Cytosacpe
- Load the session
 - **Verify** all the clusters are correct


# Part 7 - Bundles

#### Step 1 - App detection
- Close Cytoscape
- Go to the CytoscapeConfiguration/3/apps/installed folder
- Delete clusterMaker2 and WordCloud (or change their file extensions to something other than .jar or .zip)
- Start Cytoscape
- Go to New Annotation Set
 - **Verify**
 - At the top of the dialog there should be two messages. A warning that clusterMaker is not installed and an error that WordCloud is not installed.
 - Each message should have a link to the app store page to download the corresponding app.
 - Verify each link opens a browser to the correct app store page.
 - Verify that the "Create Annotations" button at the bottom is disabled.
 
 






 
 
