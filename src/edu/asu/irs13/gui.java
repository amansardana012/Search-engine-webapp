package edu.asu.irs13;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JButton;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.asu.irs13.Search;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JScrollBar;

import java.awt.Cursor;
public class gui extends JFrame {

	private JPanel contentPane;
	private JTextField Input;
	private JTextArea txtarea;
	

	
	private static double [] magnitude;
	private static List finalpagerank ;
	private JButton Pagerankidf;
	private JTextField W_input;
	private JLabel lblW;
	private JScrollBar scrollBar;
	/**
	 * Launch the application.
	 */
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			
			public void run() {
				try 
				{
					 IndexReader r = IndexReader.open(FSDirectory.open(new File("index"))); 
					magnitude = new double [r.maxDoc()];
					  
					magnitude=Search.magnitude();
				
					finalpagerank=Search.Pagerank();
				   gui frame = new gui();
					frame.setVisible(true);
				} catch (Exception e) {
					
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public gui() {
		setMinimumSize(new Dimension(700, 500));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setForeground(new Color(255, 255, 255));
		contentPane.setBackground(new Color(139, 69, 19));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		
		Input = new JTextField();
		Input.setColumns(10);
		 ButtonGroup group = new ButtonGroup();
		
		 scrollBar = new JScrollBar();
			scrollBar.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			scrollBar.setAutoscrolls(true);
		
		txtarea = new JTextArea();
		txtarea.setForeground(new Color(139, 69, 19));
		txtarea.setAutoscrolls(true);
		txtarea.setLineWrap(true);
		txtarea.setEditable(false);
		txtarea.setColumns(10);
		Font font = new Font("Tahoma", Font.ITALIC, 15);
        txtarea.setFont(font);
     
		
		JButton TF_IDF = new JButton("Vector Space");
		TF_IDF.setFont(new Font("Tahoma", Font.ITALIC, 13));
		TF_IDF.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				List list = new ArrayList();
				try
				{
				IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
				if(Input.getText().isEmpty())
				{
					txtarea.setText("Please enter words to search");
					
				}
				else
				{
				list =Search.init(Input.getText(),magnitude);
				txtarea.setText("Total results found "+ list.size()+"\n");
				if(list.size()>10)
				{
					txtarea.append("Showing top 10 results "+"\n"+"*******************************"+"\n");
					for(int i=0 ; i< 10; i++) 
					{
						String d_url = r.document((int) list.get(i)).getFieldable("path").stringValue().replace("%%", "/");
						txtarea.append("Document id is "+list.get(i).toString()+" and url is "+d_url+ "\n");

				    }
			    }
				else
				{
					txtarea.append("Showing top " +list.size()+ " results "+"\n"+"*******************************"+"\n");
					for(int i=0 ; i< list.size(); i++) 
					{
						String d_url = r.document((int) list.get(i)).getFieldable("path").stringValue().replace("%%", "/");
						txtarea.append("Document id is "+ list.get(i).toString()+" and url is "+d_url+ "\n");

					}
			}
				}
				
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}});
		TF_IDF.setBackground(new Color(210, 180, 140));
		
		JButton A_H = new JButton("A/H");
		A_H.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				List list = new ArrayList();
				List authlist = new ArrayList();
				Map hubscore= new HashMap<Integer,Double>();
				Map authscore= new HashMap<Integer,Double>();
				List hublist = new ArrayList();
				try
				{
				IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
				if(Input.getText().isEmpty())
				{
					txtarea.setText("Please enter words to search");
					
				}
				else
				{
			list=Search.init(Input.getText(),magnitude);
			Search.baseset(list);
			 authscore=Search.getauthscore();
			 hubscore= Search.gethubscore();
			 authlist.addAll(authscore.keySet());
			 hublist.addAll(hubscore.keySet());
			 txtarea.setText("Top 10 authorities are"+"\n"+"*******************************"+"\n");
			 for(int i=0 ; i< 10; i++) 
				{
				 String d_url = r.document((int) authlist.get(i)).getFieldable("path").stringValue().replace("%%", "/");
				   txtarea.append("Document id is "+authlist.get(i).toString()+" and url is "+d_url+ "\n");

			    }
			 txtarea.append("Top 10 hubs are"+"\n"+"*******************************"+"\n");
			 for(int i=0 ; i< 10; i++) 
				{
				 String d_url = r.document((int) hublist.get(i)).getFieldable("path").stringValue().replace("%%", "/");
				   txtarea.append("Document id is "+hublist.get(i).toString()+" and url is "+d_url+ "\n");

			    }
			}
		}
			
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
			}});
		A_H.setFont(new Font("Tahoma", Font.ITALIC, 13));
		A_H.setBackground(new Color(210, 180, 140));
		W_input = new JTextField();
		W_input.setColumns(10);
		
		lblW = new JLabel("W");
		lblW.setForeground(new Color(255, 255, 255));
		Pagerankidf = new JButton("PageRank+IDF");
		Pagerankidf.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Map sorted= new HashMap<Integer,Double>();
				Map relevantdocs= new HashMap<Integer,Double>();
				List doclist= new ArrayList();
				
				try
				{
					if(Input.getText().isEmpty())
					{
						txtarea.setText("Please enter words to search");
					}
					if(W_input.getText().isEmpty())
					{
						txtarea.setText("Please enter words to search/the Value of W");
					}
					
				double Wvalue=Double.parseDouble(W_input.getText());
				if(Wvalue<=0.0|| Wvalue>=1.0)
				{
					txtarea.setText("Please enter a Valid value for W");
				}
				
				else 
				{
					IndexReader r = IndexReader.open(FSDirectory.open(new File("index"))); 	
			       Search.init(Input.getText(),magnitude);
				
			    sorted=Search.getsorteddocs();
			    
			relevantdocs=Search.printdocswithrank(sorted, finalpagerank,Wvalue);
			doclist.addAll(relevantdocs.keySet());
			if(doclist.size()>0)
			{
			txtarea.setText("Total results found "+ doclist.size()+"\n");
			if(doclist.size()>10)
			{
				txtarea.append("Showing top 10 results "+"\n"+"*******************************"+"\n");
				for(int i=0 ; i< 10; i++) 
				{
		      String d_url = r.document((int) doclist.get(i)).getFieldable("path").stringValue().replace("%%", "/");
			   txtarea.append("Document id is "+doclist.get(i).toString()+" and url is "+d_url+ "\n");
			    }
		    }
			else
			{
				txtarea.append("Showing top " +doclist.size()+ " results "+"\n"+"*******************************"+"\n");
				for(int i=0 ; i< doclist.size(); i++) 
				{
					String d_url = r.document((int) doclist.get(i)).getFieldable("path").stringValue().replace("%%", "/");
					   txtarea.append("Document id is "+doclist.get(i).toString()+" and url is "+d_url+ "\n");

				}
			
			}
					}	
				}}
				catch (Exception ex)
				{
			     
				}
			}
		});
		Pagerankidf.setFont(new Font("Tahoma", Font.ITALIC, 13));
		Pagerankidf.setBackground(new Color(210, 180, 140));
		
		
		
		
	
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addComponent(Input, GroupLayout.DEFAULT_SIZE, 671, Short.MAX_VALUE)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addContainerGap()
							.addComponent(TF_IDF, GroupLayout.PREFERRED_SIZE, 113, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(A_H, GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(Pagerankidf, GroupLayout.PREFERRED_SIZE, 124, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(W_input, GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(lblW)
							.addGap(190)))
					.addGap(19))
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addComponent(scrollBar, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(txtarea)
					.addContainerGap())
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addComponent(Input, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(15)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE, false)
						.addComponent(TF_IDF)
						.addComponent(A_H)
						.addComponent(Pagerankidf)
						.addComponent(W_input, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblW))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addComponent(txtarea, GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
						.addComponent(scrollBar, GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE))
					.addContainerGap())
		);
		contentPane.setLayout(gl_contentPane);
		
	}
}
