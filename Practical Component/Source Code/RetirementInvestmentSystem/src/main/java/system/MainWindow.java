package system;

/*
Project: Retirement Investment Optimization Using Algorithmic Design
Course: CIT3003 - Algorithms & Data Structures
Date: November 2025

PROJECT DESCRIPTION:
--------------------
This software application is designed for AofA Financial Services Ltd. to solve
complex financial optimization problems regarding retirement planning. The core
functionality simulates investment growth under fixed and variable interest rates.
Crucially, it utilizes an Algorithmic Design approach—specifically Binary Search
(Successive Approximation)—to determine the "Maximum Sustainable Withdrawal."
This ensures a retiree's funds reach exactly zero at the end of their target
lifespan, optimizing their spending power without the risk of premature depletion.


AI USAGE STATEMENT (Rubric Criterion #5):
-----------------------------------------
This code was developed with the assistance of Microsoft Copilot.
The AI was used to:
1. Structure the Java class hierarchy for modularity.
2. Debug the Binary Search logic to ensure convergence using epsilon.
3. Implement the JFreeChart visualization fallback mechanism.
The algorithmic logic and complexity analysis remain the work of the students.
*/


import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.geom.Ellipse2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class MainWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private java.util.Set<Integer> invalidRowSet = new java.util.HashSet<>();

	static double principal;
	static double rate;
	double endBalance;
	double expense;
	double balance;
	static int years; 
    private static DefaultTableModel model;

	
	private static double fixedInvestor(double principal, double rate, int years)
	{
		/*
        Simulates compound interest with a fixed rate.
        Complexity: O(T) - Linear Time
        Returns: (balance)
        */
		
		double balance = principal;
		for (int i = 1; i <= years; i++)
		{
			balance = balance * (1 + rate);
		}
		
		return balance;		  
	}
	
	
	private double variableInvestor(double principal, double[] rate_list)
	{
		/*
	    Simulates growth based on a list of changing rates.
	    Complexity: O(N) - Linear Time based on list size
	    Returns: (balance)
	    """*/
		double balance = principal; 
	    for (double rate : rate_list)
	    {
	        balance = balance * (1 + rate);
	    }	 
	    return balance;
	}
	

	//This follows a withdraw first, then grow approach
	public static int finallyRetired(double balance, double annualExpense, double rate, int capYears) 
	{
	
		/*
        Determines how many years funds will last given a specific withdrawal.
        Includes a 'cap_years' to prevent infinite loops if interest > expense.
        Returns: (years_lasted, history_list)
        """
        */
	    int years = 0;
	    while (balance > 0 && years < capYears) {
	        // Withdraw at start of year
	        balance -= annualExpense;
	
	        if (balance > 0) {
	            // Grow remainder
	            balance += balance * rate;
	        } else {
	            balance = 0; // floor at zero for cleanliness
	        }
	
	        years++;
	    }
	
	    return years ;
	}
	
	public static double maximumExpensed(double balance, double rate, int targetYears) {
		
		 /* OPTIMIZATION ALGORITHM: Binary Search (Divide & Conquer).
        Finds the optimal withdrawal amount to last exactly 'target_years'.

        Why this works: The relationship between Withdrawal Amount and Years Lasted
        is Monotonic (Decreasing). This allows us to cut the search space in half
        iteratively rather than guessing linearly.

        Complexity: O(log N)
        
        Returns: (low)
        */

	    double low = 0.0;
	    double high = balance;      // Upper bound (withdraw everything in year 1)
	    double epsilon = 0.01;      // Precision to 1 cent

	    // Binary Search Loop
	    while ((high - low) > epsilon) {
	        double mid = (high + low) / 2.0;

	        // Run simulation using mid as the withdrawal amount
	        int yearsLasted = finallyRetired(balance, mid, rate, 120);

	        if (yearsLasted < targetYears) {
	            // Money ran out too fast → spending too much
	            high = mid;
	        } else {
	            // Money lasted too long or just right → can spend more
	            low = mid;
	        }
	    }

	    return low; // best estimate
	}

	    
		  
	
	public MainWindow()
	{
		setTitle("Retirement Investment System");
		setSize(700,500);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	    setLocationRelativeTo(null);
	
		 CardLayout cardLayout = new CardLayout();
		
		 
		 //Creates a main JPanel object with a primary constructor that accepts a CardLayout
		 //This CardLayout would allow the user to switch pages using mainPanel object
		 JPanel mainPanel = new JPanel(cardLayout);
		
		
		 //Creates JPanel objects with a primary constructor that accepts a new GridBagLayout
		 JPanel homePage = new JPanel(new GridBagLayout());
		 JPanel fixedGrowthPage = new JPanel(new BorderLayout());
		 JPanel variableGrowthPage = new JPanel(new BorderLayout());
		 JPanel retireExpenPage = new JPanel(new BorderLayout());
		 JPanel optimizePage = new JPanel(new BorderLayout());


		//Splits Fixed Investment Page into left and right sections

		JPanel left = new JPanel(new GridBagLayout());   // form
		JPanel right = new JPanel(new GridBagLayout());   // results
		
		fixedGrowthPage.add(left, BorderLayout.WEST);
		fixedGrowthPage.add(right, BorderLayout.CENTER);
		
		left.setPreferredSize(new Dimension(300, 400));

		//Splits Variable Investment Page into left and right sections

		JPanel leftVar = new JPanel(new GridBagLayout());   // form
		JPanel rightVar = new JPanel(new GridBagLayout());   // results
		
		variableGrowthPage.add(leftVar, BorderLayout.WEST);
		variableGrowthPage.add(rightVar, BorderLayout.CENTER);
		
		// Constrain widths by wrapping left in a fixed size container

		leftVar.setPreferredSize(new Dimension(300, 400));
		
		
		//Splits Retirement Expense Page into left and right sections
		JPanel leftExpen = new JPanel(new GridBagLayout());   // form
		JPanel rightExpen = new JPanel(new GridBagLayout());   // results
		
		retireExpenPage.add(leftExpen, BorderLayout.WEST);
		retireExpenPage.add(rightExpen, BorderLayout.CENTER);
		
		// Constrain widths by wrapping left in a fixed size container

		leftExpen.setPreferredSize(new Dimension(300, 400));
		
		//Splits Optimize Withdrawal Page into left and right sections
		JPanel leftOp = new JPanel(new GridBagLayout());   // form
		JPanel rightOp = new JPanel(new GridBagLayout());   // results
				
		optimizePage.add(leftOp, BorderLayout.WEST);
		optimizePage.add(rightOp, BorderLayout.CENTER);
		
		// Constrain widths by wrapping left in a fixed size container

		leftOp.setPreferredSize(new Dimension(300, 400));


		//Adds Pages to the Main
		
		mainPanel.add(homePage, "Menu");
		mainPanel.add(fixedGrowthPage, "FixedGrowth");
		mainPanel.add(variableGrowthPage,"VariableGrowth");
		mainPanel.add(retireExpenPage,"RetirementExpense");
		mainPanel.add(optimizePage, "OptimizeWithdrawal");

		add(mainPanel);
	    cardLayout.show(mainPanel, "Menu");

	    //This creates a label with the title of this project
		JLabel topic = new JLabel("AofA Financial Services - Investment Optimization",SwingConstants.CENTER);
		topic.setFont(new Font("Arial", Font.BOLD, 20));
		
		JLabel project = new JLabel("Algorithmic Design Project (CIT3003)",SwingConstants.CENTER);
		project.setFont(new Font("Arial", Font.BOLD, 20));
		
		

		
		JButton fixedGrowthBtn = new JButton("Fixed Growth");
		// Sets the background of button to dark-green
		fixedGrowthBtn.setBackground( new Color(0, 0, 139));
		//Sets the text of  button to  white

		fixedGrowthBtn.setForeground(Color.white);
		
		//Sets the background to dark-blue and white for buttons 
		//These buttons when clicked allows the mainPanel to use the CardLayout to switch between pages
		JButton variableGrowthBtn = new JButton("Variable Growth");
		variableGrowthBtn.setBackground( new Color(0, 0, 139));
		variableGrowthBtn.setForeground(Color.white);
		JButton retDeplBtn = new JButton("Retirement Expense");
		retDeplBtn.setBackground( new Color(0, 0, 139));
		retDeplBtn.setForeground(Color.white);
		JButton optiWithdrawlBtn = new JButton("Optimize Withdrawal");
		optiWithdrawlBtn.setBackground( new Color(0, 0, 139));
		optiWithdrawlBtn.setForeground(Color.white);
		
		
		GridBagConstraints gc = new GridBagConstraints();
 		gc = new GridBagConstraints();
 		gc.insets = new Insets(10, 10, 10, 10);
        gc.fill = GridBagConstraints.HORIZONTAL;
		
        
        //Uses addToGridBag method to add components to Home Page
        addToGridBag(homePage,topic,gc,0,0,4,1);
        addToGridBag(homePage,project,gc,0,1,4,1);
        addToGridBag(homePage,fixedGrowthBtn,gc,0,2,1,1);
        addToGridBag(homePage,variableGrowthBtn,gc,1,2,1,1);
        addToGridBag(homePage,retDeplBtn,gc,2,2,1,1);
        addToGridBag(homePage,optiWithdrawlBtn,gc,3,2,1,1);

        //Switches from the Home Page to Fixed Growth Page
        fixedGrowthBtn.addActionListener(e ->
        {
        	cardLayout.show(mainPanel, "FixedGrowth");
        });
        
        //Switches from the Home Page to Variable Growth Page

        variableGrowthBtn.addActionListener(e ->{
        	cardLayout.show(mainPanel, "VariableGrowth");
        });
        
        //Switches from the Home Page to Retirement Expense Page

        
        retDeplBtn.addActionListener(e ->{
        	cardLayout.show(mainPanel, "RetirementExpense");
        });
        
        //Switches from the Home Page to Optimize Withdrawal Page

        optiWithdrawlBtn.addActionListener(e ->{
        	cardLayout.show(mainPanel, "OptimizeWithdrawal");
        });
        
        //Initializes left and right side of Fixed Growth Page Components
                
        
        JLabel fTitle = new JLabel("Fixed Growth Calculator", SwingConstants.CENTER);
		fTitle.setFont(new Font("Arial", Font.BOLD, 20));

        
		JLabel initialValueLabel = new JLabel("Initial Investment: ");
		JTextField initialValueField = new JTextField(15);
		
		JLabel interestLabel = new JLabel("Interest Rate: ");
		JTextField interestField = new JTextField(15);
		
		JLabel percentage = new JLabel("%");
		percentage.setFont(new Font("Arial", Font.BOLD, 13));

		JLabel yearLabel = new JLabel("Years of Investment: ");
		JTextField yearField = new JTextField(15);
		
		JButton calculateFixedGrowth = new JButton("Calculate");
		calculateFixedGrowth.setBackground(new Color(0, 100, 0));
		calculateFixedGrowth.setForeground(Color.white);
		
		JButton clearfBtn = new JButton("Clear");
		clearfBtn.setBackground(Color.DARK_GRAY);
		clearfBtn.setForeground(Color.white);
		
		JButton backToMenuF = new JButton("Back to Menu");
		backToMenuF.setBackground( new Color(0, 0, 139));
		backToMenuF.setForeground(Color.white);
		
		JLabel resultsLabel = new JLabel("Results");
	    resultsLabel.setFont(new Font("Arial", Font.BOLD, 20));
	        
	    JLabel endBalLabel = new JLabel("Ending Balance", SwingConstants.CENTER);
	    endBalLabel.setFont(new Font("Arial",Font.BOLD, 15));
	        
	    JLabel endBalValue = new JLabel("$", SwingConstants.CENTER);
	    endBalValue.setFont(new Font("Arial",Font.BOLD, 15));
	        
	    JLabel graphF = new JLabel("Graph");
	    graphF.setFont(new Font("Arial", Font.BOLD, 20));
		
        //Uses addToGridBag method to add components to left side of Fixed Growth Page

		GridBagConstraints gc2 = new GridBagConstraints();
 		gc2 = new GridBagConstraints();
 		gc2.insets = new Insets(10, 10, 10, 10);
        gc2.fill = GridBagConstraints.HORIZONTAL;
        
        addToGridBag(left,fTitle,gc2,0,0,2,1);
        addToGridBag(left,initialValueLabel,gc2,0,1,1,1);
        addToGridBag(left,initialValueField,gc2,1,1,1,1);

        addToGridBag(left,interestLabel,gc2,0,2,1,1);
        addToGridBag(left,interestField,gc2,1,2,1,1);
        addToGridBag(left,percentage,gc2,2,2,1,1);


        addToGridBag(left,yearLabel,gc2,0,3,1,1);
        addToGridBag(left,yearField,gc2,1,3,1,1);
        
        addToGridBag(left,calculateFixedGrowth,gc2,0,4,2,1);
        addToGridBag(left,clearfBtn,gc2,0,5,2,1);
        addToGridBag(left,backToMenuF,gc2,0,6,2,1);

 
        //Initializes JScrollPane objects for all pages
        //sets properties to allow nice scrolling
	    // Scroll container in CENTER of right panel
	     JScrollPane chartScrollPane = new JScrollPane();
	     chartScrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	     chartScrollPane.getVerticalScrollBar().setUnitIncrement(16); 

	     JScrollPane variableScrollPane = new JScrollPane();
	     variableScrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	     variableScrollPane.getVerticalScrollBar().setUnitIncrement(16);

	     JScrollPane expenseScrollPane = new JScrollPane();
	     expenseScrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	     expenseScrollPane.getVerticalScrollBar().setUnitIncrement(16); 
	     
	     JScrollPane optimizeScrollPane = new JScrollPane();
	     optimizeScrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	     optimizeScrollPane.getVerticalScrollBar().setUnitIncrement(16); 
	     
	     
	    //Uses addToGridBag method to add components to right side of Fixed Growth Page

		GridBagConstraints gc3 = new GridBagConstraints();
 		gc3 = new GridBagConstraints();
 		gc3.insets = new Insets(10, 10, 10, 10);
        gc3.fill = GridBagConstraints.HORIZONTAL;
        
        addToGridBag(right,resultsLabel,gc3,0,0,1,1);
        addToGridBag(right,endBalLabel,gc3,0,1,1,1);
        addToGridBag(right,endBalValue,gc3,1,1,1,1);
        addToGridBag(right,graphF,gc3,0,2,1,1);
        
        gc3.gridx = 0;
		gc3.gridy = 7;
		gc3.gridwidth = 2;            // span both input columns
		gc3.gridheight = 2;           // let it occupy multiple rows
		gc3.insets = new Insets(10, 10, 10, 10);
		gc3.weightx = 1.0;            // allow horizontal growth
		gc3.weighty = 1.0;            // allow vertical growth
		gc3.fill = GridBagConstraints.BOTH;

		right.add(chartScrollPane, gc3);


        
        calculateFixedGrowth.addActionListener(e ->{
        	
        	//Validation Check to see if one or more fields are empty. 
        	//It will prevent submission of form and alert the user to fill in field/s
        	
        	if(initialValueField.getText().compareTo("") == 0 || interestField.getText().compareTo("") == 0 || yearField.getText().compareTo("") == 0 )
        	{
        		JOptionPane.showMessageDialog(fixedGrowthPage, "One or more fields were not filled\nPlease check to see you entered all values.", "Warning", JOptionPane.WARNING_MESSAGE);
        		return;
        	}
        	
        	//Validation Check to see if the values in the fields are in number format
        	
        	try
        	{
        		 balance = Double.parseDouble(initialValueField.getText());
            	 rate = Double.parseDouble(interestField.getText());
            	 years  = Integer.parseInt(yearField.getText());
        	}
        	//Prevents submission of form if field/s are found to not be in number format
        	catch(NumberFormatException nf)
        	{
        		JOptionPane.showMessageDialog(fixedGrowthPage, "Invalid datatype. All fields must be in number format.", "Warning", JOptionPane.WARNING_MESSAGE);

        		return;
        	}
        	//Validation check to see if balance, rate, years are non-negative numbers
        	if(balance <= 0)
        	{
        		JOptionPane.showMessageDialog(fixedGrowthPage, "Initial Investment must be a postive number (numbers greater than 0)", "Warning", JOptionPane.WARNING_MESSAGE);
        		return;
        	}
        	

        	
        	if(rate <= 0)
        	{
        		JOptionPane.showMessageDialog(fixedGrowthPage, "Interest Rate must be a postive number (numbers greater than 0)", "Warning", JOptionPane.WARNING_MESSAGE);
        		return;
        	}
        	
        	if (rate > 1000) 
        	{ 
    			
        		JOptionPane.showMessageDialog(fixedGrowthPage, "Rate is too large. Please use 1000 or less.", "Warning", JOptionPane.ERROR_MESSAGE);
    		   
    		    System.err.println("Rate is value too large. Please use 1000 or less.");
        		return;
        	}
        	
        	
        	if(years <= 0)
        	{
        		JOptionPane.showMessageDialog(fixedGrowthPage, "Years must be a postive number (numbers greater than 0)", "Warning", JOptionPane.WARNING_MESSAGE);
        		return;
        	}
        	
        	if (years > 1000) { // or any sensible limit
    			
        		JOptionPane.showMessageDialog(fixedGrowthPage, "Target Years is too large. Please use 1000 or less.", "Warning", JOptionPane.ERROR_MESSAGE);
    		   
    		    System.err.println("Target years is value too large. Please use 1000 or less.");
        		return;
        	}
        	
        	//Converts whole numbers into decimal format if it is > 1
        	//This allows both whole numbers and decimals to be used
        	if (rate >= 1) {
                rate /= 100.0;
            }
        	
        	
        	endBalance = fixedInvestor(balance, rate, years);
        	
        	endBalValue.setText("$" + String.format("%.2f",endBalance));
        	


        	//Builds and create chart upon successful submission
			JFreeChart chart = buildFixedInvestmentChart(balance, rate, years);
			ChartPanel chartPanel = new org.jfree.chart.ChartPanel(chart);
			chartPanel.setPreferredSize(new java.awt.Dimension(600, 400));
			chartPanel.setMouseWheelEnabled(true);
			
			// Put inside your scroll pane (keeping finalYear label intact)
			
			    chartScrollPane.setViewportView(chartPanel);
			    chartScrollPane.revalidate();
			    chartScrollPane.repaint();
		

        	
        });
        
        
        //Clears Components of Fixed Growth PAGE
        //Placeholder label is set for the chart
        clearfBtn.addActionListener(e ->{
        	
        	
        	initialValueField.setText("");
        	yearField.setText("");
        	interestField.setText("");
        	endBalValue.setText("$");
        	

        	JPanel placeholder = new JPanel(new GridBagLayout());
		    JLabel msg = new JLabel("No chart to display. Enter values and click Calculate.",
		                            SwingConstants.CENTER);
		    msg.setFont(new Font("Arial", Font.ITALIC, 14));
		    placeholder.add(msg, new GridBagConstraints());
		    chartScrollPane.setViewportView(placeholder);
		
		    // 4) Refresh UI
		    right.revalidate();
		    right.repaint();

        });

        backToMenuF.addActionListener(e ->{
        	cardLayout.show(mainPanel, "Menu");
        	clearfBtn.doClick();
        });
        

     //Initializes left and right side of Variable Growth Page Components


     JLabel titleV = new JLabel("Variable Growth Calculator", SwingConstants.CENTER);
     titleV.setFont(new Font("Arial", Font.BOLD, 20));

     JLabel principalLabel = new JLabel("Initial Investment: ");
     JTextField principalField = new JTextField(15);

     JLabel yearVLabel = new JLabel("Years of Investment: ");
     JTextField yearVField = new JTextField(15);

     JButton calcuBtn = new JButton("Calculate");
     calcuBtn.setBackground(new Color(0, 100, 0));
     calcuBtn.setForeground(Color.white);

     JButton clearvBtn = new JButton("Clear");
     clearvBtn.setBackground(Color.DARK_GRAY);
     clearvBtn.setForeground(Color.white);

     JButton backToMenuV = new JButton("Back to Menu");
     backToMenuV.setBackground(new Color(0, 0, 139));
     backToMenuV.setForeground(Color.white);
     
     JLabel resultsVLabel = new JLabel("Results");
     resultsVLabel.setFont(new Font("Arial", Font.BOLD, 20));
     
     JLabel endBalVLabel = new JLabel("Ending Balance", SwingConstants.CENTER);
     endBalVLabel.setFont(new Font("Arial",Font.BOLD, 15));
     
     JLabel endBalValueV = new JLabel("$", SwingConstants.CENTER);
     endBalValueV.setFont(new Font("Arial",Font.BOLD, 15));
     
     JLabel graphV = new JLabel("Graph");
     graphV.setFont(new Font("Arial", Font.BOLD, 20));
     
     
     

     JButton generateBtn = new JButton("Generate Rate Table");


     model = new DefaultTableModel(new Object[]{"Year", "Rate (%)"}, 0);


     JTable rateTable = new JTable(model);
     rateTable.setFillsViewportHeight(true);
     rateTable.setRowHeight(24);
     rateTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

     // Optional: nicer column widths
     rateTable.getColumnModel().getColumn(0).setPreferredWidth(80);
     rateTable.getColumnModel().getColumn(1).setPreferredWidth(150);

     // Place table in a scroll pane (only once!)
     JScrollPane tableScroll = new JScrollPane(rateTable);
     tableScroll.setPreferredSize(new Dimension(400, 180));

     // --- Layout constraints for the left panel (variable growth) ---
     GridBagConstraints gc4 = new GridBagConstraints();
     gc4.insets = new Insets(10, 10, 10, 10);
     gc4.fill = GridBagConstraints.HORIZONTAL;
     
     // --- Add components to leftVar (your left side panel for variable growth) ---
     addToGridBag(leftVar, titleV,         gc4, 0, 0, 2, 1);
     addToGridBag(leftVar, principalLabel, gc4, 0, 1, 1, 1);
     addToGridBag(leftVar, principalField, gc4, 1, 1, 1, 1);

     addToGridBag(leftVar, yearVLabel,     gc4, 0, 2, 1, 1);
     addToGridBag(leftVar, yearVField,     gc4, 1, 2, 1, 1);

     addToGridBag(leftVar, generateBtn,    gc4, 0, 3, 2, 1);
     
     addToGridBag(leftVar, calcuBtn,    gc4, 0, 4, 2, 1);
     addToGridBag(leftVar, clearvBtn,    gc4, 0, 5, 2, 1);
     addToGridBag(leftVar, backToMenuV,    gc4, 0, 6, 2, 1);



     // Keep the table spanning two columns (0..1) and multiple rows.
     // If you truly need three columns, ensure your layout has room.
     
     

		
		gc4.gridx = 0;
		gc4.gridy = 7;
		gc4.gridwidth = 2;            // span both input columns
		gc4.gridheight = 2;           // let it occupy multiple rows
		gc4.insets = new Insets(10, 10, 10, 10);
		gc4.weightx = 1.0;            // allow horizontal growth
		gc4.weighty = 1.0;            // allow vertical growth
		gc4.fill = GridBagConstraints.BOTH;

     

	leftVar.add(tableScroll, gc4);

    
     // --- Add components to rightVar (your right side panel for variable growth) ---

     
     GridBagConstraints gc5 = new GridBagConstraints();
     gc5.insets = new Insets(10, 10, 10, 10);
     gc5.fill = GridBagConstraints.HORIZONTAL;
     
     
     addToGridBag(rightVar,resultsVLabel,gc5,0,0,1,1);
     addToGridBag(rightVar,endBalVLabel,gc5,0,1,1,1);
     addToGridBag(rightVar,endBalValueV,gc5,1,1,1,1);
     addToGridBag(rightVar,graphV,gc5,0,2,1,1);
     
     	gc5.gridx = 0;
		gc5.gridy = 3;
		gc5.gridwidth = 2;            // span both input columns
		gc5.gridheight = 2;           // let it occupy multiple rows
		gc5.insets = new Insets(10, 10, 10, 10);
		gc5.weightx = 1.0;            // allow horizontal growth
		gc5.weighty = 1.0;            // allow vertical growth
		gc5.fill = GridBagConstraints.BOTH;
		
		rightVar.add(variableScrollPane, gc5);



calcuBtn.addActionListener(e -> {
    commitTableEdits(rateTable);

    double principal;
    try {
        principal = Double.parseDouble(principalField.getText().trim());
        if (principal < 0) throw new NumberFormatException("Principal must be non-negative.");
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(principalField,
                "Please enter a valid, non-negative Initial Investment.",
                "Invalid Input", JOptionPane.ERROR_MESSAGE);
        principalField.requestFocusInWindow();
        return;
    }

    int rows = model.getRowCount();
    if (rows == 0) {
        JOptionPane.showMessageDialog(rateTable,
                "No rates found. Click 'Generate Rate Table' and enter rates.",
                "Missing Data", JOptionPane.WARNING_MESSAGE);
        return;
    }

    double[] rateList = new double[rows];
    java.util.List<Integer> badRows = new java.util.ArrayList<>();
    StringBuilder errors = new StringBuilder();

    for (int r = 0; r < rows; r++) {
        Object rateObj = model.getValueAt(r, 1);
        if (rateObj == null) {
            errors.append(String.format("Row %d: Rate is empty.%n", r + 1));
            badRows.add(r);
            continue;
        }
        double rateVal;
        try {
            rateVal = (rateObj instanceof Number)
                    ? ((Number) rateObj).doubleValue()
                    : Double.parseDouble(rateObj.toString().trim());
        } catch (NumberFormatException ex2) {
            errors.append(String.format("Row %d: Rate '%s' is not numeric.%n", r + 1, rateObj));
            badRows.add(r);
            continue;
        }

        if (rateVal < 0.0 || rateVal > 1000) {
	         errors.append(String.format("Row %d: Rate %.2f out of range [0, 1000].%n", r + 1, rateVal));
	         badRows.add(r);
	         continue;
	     }
        
        if (rateVal >= 1) {
            rateVal /= 100.0;
        }

	     // Bounds: decimal format in [0.0, 1.0] → 0%..100% (non-negative only)
	     if (Double.isNaN(rateVal) || Double.isInfinite(rateVal)) {
	         errors.append(String.format("Row %d: Rate must be a finite number.%n", r + 1));
	         badRows.add(r);
	         continue;
	     }
	     

        rateList[r] = rateVal;
    }

    if (!badRows.isEmpty()) {
        highlightInvalidRows(rateTable, badRows);
        JOptionPane.showMessageDialog(rateTable, errors.toString(),
                "Validation Errors", JOptionPane.ERROR_MESSAGE);
        return;
    } else {
        clearHighlight(rateTable);
    }

    double finalBalance = variableInvestor(principal, rateList);
    endBalValueV.setText(String.format("$%,.2f", finalBalance));

    ChartPanel chartPanel = buildVariableXYChartFromRates(principal, rateList);
    variableScrollPane.setViewportView(chartPanel);
    right.revalidate();
    right.repaint();
});


	// Action: generate a rate table template for the given number of years
	generateBtn.addActionListener(e -> {
	    String yearsText = yearVField.getText().trim();
	    if (yearsText.isEmpty()) {
	        JOptionPane.showMessageDialog(
	            leftVar,
	            "Please enter the number of years.",
	            "Input Required",
	            JOptionPane.WARNING_MESSAGE
	        );
	        return;
	    }
	
	    int years;
	    try {
	        years = Integer.parseInt(yearsText);
	        if (years <= 0 ) {
	            throw new NumberFormatException("Years must be positive.");
	        }
	        if(years >  1000)
	        {
	            throw new NumberFormatException("Years cannot exceed 1000.");

	        }
	    } catch (NumberFormatException ex) {
	        JOptionPane.showMessageDialog(
	            leftVar,
	            "Please enter a valid positive integer for years.",
	            "Invalid Input",
	            JOptionPane.ERROR_MESSAGE
	        );
	        return;
	    }
	
	    // Clear old data
	    model.setRowCount(0);
	
	    // Populate rows: Year = 1..years, Rate default 0.00
	    for (int i = 1; i <= years; i++) {
	        model.addRow(new Object[] { i, 0.00 });
	    }
	
	    // Optional: move focus to the first rate cell for quick editing
	    if (years > 0) {
	        rateTable.requestFocusInWindow();
	        rateTable.changeSelection(0, 1, false, false);
	        rateTable.editCellAt(0, 1);
	    }
	});


	clearvBtn.addActionListener(e -> {
	    // 1) Clear input fields
	    principalField.setText("");
	    yearVField.setText("");
	
	    // 2) Clear table data
	    model.setRowCount(0);
	
	    // 3) Reset Ending Balance label
	    endBalValueV.setText("$");
	
	    // 4) Remove chart (or show a placeholder)
	    // Option A: remove any chart
	    variableScrollPane.setViewportView(null);
	
	    // Option B: show a friendly placeholder message
	    
	    JPanel placeholder = new JPanel(new GridBagLayout());
	    JLabel msg = new JLabel("No chart to display. Enter values and click Calculate.",
	                            SwingConstants.CENTER);
	    msg.setFont(new Font("Arial", Font.ITALIC, 14));
	    placeholder.add(msg, new GridBagConstraints());
	    variableScrollPane.setViewportView(placeholder);
	   
	
	    // 5) Clear any validation highlighting
	    clearHighlight(rateTable);
	
	    // 6) Refresh UI
	    right.revalidate();
	    right.repaint();
	
	    // 7) (Optional) Return focus to the first field
	    principalField.requestFocusInWindow();
	});


     // Back to menu
     backToMenuV.addActionListener(e -> {
    	 clearvBtn.doClick();
         cardLayout.show(mainPanel, "Menu");
     });


     //Initializes Components of Retirement Depletion Page
     JLabel titleE = new JLabel("Retirement Depletion", SwingConstants.CENTER);
     JLabel calcLabel = new JLabel("Calculator", SwingConstants.CENTER);
     calcLabel.setFont(new Font("Arial", Font.BOLD, 20));
     titleE.setFont(new Font("Arial", Font.BOLD, 20));
     
     JLabel balanceLabel = new JLabel("Balance");
     JTextField balanceField = new JTextField(15);
     
     JLabel expenseLabel = new JLabel("Expense");
     JTextField expenseField = new JTextField(15);
     
     JLabel rateLabel = new JLabel("Rate");
     JTextField rateField = new JTextField(15);
     
     JLabel percentageRate = new JLabel("%");
     percentageRate.setFont(new Font("Arial", Font.BOLD, 13));

     
     JButton calculate = new JButton("Calculate");
     calculate.setBackground(new Color(0, 100, 0));
     calculate.setForeground(Color.white);
		
	JButton clearEBtn = new JButton("Clear");
	clearEBtn.setBackground(Color.DARK_GRAY);
	clearEBtn.setForeground(Color.white);
		
	JButton backToMenuE = new JButton("BacktoMenu");
	backToMenuE.setBackground( new Color(0, 0, 139));
	backToMenuE.setForeground(Color.white);
		
	JLabel resultsLabelE = new JLabel("Result", SwingConstants.CENTER);
	resultsLabelE.setFont(new Font("Arial", Font.BOLD, 20));
	
	JLabel finalYearLabel = new JLabel("Years: ", SwingConstants.CENTER);
	finalYearLabel.setFont(new Font("Arial", Font.BOLD, 20));
	JLabel finalYearValue = new JLabel("");
	finalYearValue.setFont(new Font("Arial", Font.BOLD, 20));

	JLabel graphE = new JLabel("Graph", SwingConstants.CENTER);
	graphE.setFont(new Font("Arial", Font.BOLD, 20));
     
     GridBagConstraints gc6 = new GridBagConstraints();
     gc6.insets = new Insets(10, 10, 10, 10);
     gc6.fill = GridBagConstraints.HORIZONTAL;
     
     //Uses addToGridBag method to add components to left side of Retirement Depletion Page

     
    addToGridBag(leftExpen,titleE,gc6,0,0,3,1);
    addToGridBag(leftExpen,calcLabel,gc6,0,1,3,1);

    addToGridBag(leftExpen,balanceLabel,gc6,0,2,1,1);
    addToGridBag(leftExpen,balanceField,gc6,1,2,1,1);
    
    addToGridBag(leftExpen,expenseLabel,gc6,0,3,1,1);
    addToGridBag(leftExpen,expenseField,gc6,1,3,1,1);
    addToGridBag(leftExpen,rateLabel,gc6,0,4,1,1);
    addToGridBag(leftExpen,rateField,gc6,1,4,1,1);
    addToGridBag(leftExpen,percentageRate,gc6,2,4,1,1);

    addToGridBag(leftExpen,calculate,gc6,1,5,1,1);
    addToGridBag(leftExpen,clearEBtn,gc6,1,6,1,1);
    addToGridBag(leftExpen,backToMenuE,gc6,1,7,1,1);
    
    GridBagConstraints gc7 = new GridBagConstraints();
    gc7.insets = new Insets(10, 10, 10, 10);
    gc7.fill = GridBagConstraints.HORIZONTAL;
    
    //Uses addToGridBag method to add components to right side of Fixed Growth Page

    
    addToGridBag(rightExpen,resultsLabelE,gc7,0,0,1,1);

    addToGridBag(rightExpen,finalYearLabel,gc7,0,1,1,1);
    addToGridBag(rightExpen,finalYearValue,gc7,1,1,1,1);
    addToGridBag(rightExpen,graphE,gc7,0,2,1,1);


    gc7.gridx = 0;
	gc7.gridy = 3;
	gc7.gridwidth = 2;            // span both input columns
	gc7.gridheight = 2;           // let it occupy multiple rows
	gc7.insets = new Insets(10, 10, 10, 10);
	gc7.weightx = 1.0;            // allow horizontal growth
	gc7.weighty = 1.0;            // allow vertical growth
	gc7.fill = GridBagConstraints.BOTH;
	
	rightExpen.add(expenseScrollPane, gc7);
    

     

    calculate.addActionListener(e ->{
    	
    	//Validation Check to see if one or more fields are empty. 
    	//It will prevent submission of form and alert the user to fill in field/s    	
    	
    	if(balanceField.getText().compareTo("") ==0 || expenseField.getText().compareTo("") == 0 || rateField.getText().compareTo("") == 0)
    	{
    		JOptionPane.showMessageDialog(retireExpenPage, "One or more fields are empty", "Warning", JOptionPane.WARNING_MESSAGE);
    		return;
    	}
    	//Validation Check to see if the values in the fields are in number format
    	try
    	{
    		 principal = Double.parseDouble(balanceField.getText());
        	 expense = Double.parseDouble(expenseField.getText());
        	 rate  = Double.parseDouble(rateField.getText());
        	 
    	}
    	//Prevents calculation of contents if fields are not in number format
    	catch(NumberFormatException nf)
    	{
    		JOptionPane.showMessageDialog(fixedGrowthPage, "Invalid datatype. All fields must be in number format.", "Warning", JOptionPane.WARNING_MESSAGE);
    		return;
    	}
    	
    	//Validation check to see if balance, rate, years are non-negative numbers
    	if(principal <= 0)
    	{
    		JOptionPane.showMessageDialog(fixedGrowthPage, "Initial Investment must be a postive number (numbers greater than 0)", "Warning", JOptionPane.WARNING_MESSAGE);
    		return;
    	}
    	
    	if(rate <= 0)
    	{
    		JOptionPane.showMessageDialog(fixedGrowthPage, "Interest Rate must be a postive number (numbers greater than 0)", "Warning", JOptionPane.WARNING_MESSAGE);
    		return;
    	}
    	
    	if (rate > 1000) 
    	{ 
			
    		JOptionPane.showMessageDialog(fixedGrowthPage, "Rate is too large. Please use 1000 or less.", "Warning", JOptionPane.ERROR_MESSAGE);
		   
		    System.err.println("Rate is value too large. Please use 1000 or less.");
    		return;
    	}
    	
    	
    	if(expense <= 0)
    	{
    		JOptionPane.showMessageDialog(fixedGrowthPage, "Expense must be a postive number (numbers greater than 0)", "Warning", JOptionPane.WARNING_MESSAGE);
    		return;
    	}
    	
    	if(expense > principal)
    	{
    		JOptionPane.showMessageDialog(fixedGrowthPage, "Expense cannot be higher than balance\nPlease enter either a lower expense or higher balance", "Warning", JOptionPane.WARNING_MESSAGE);
    		return;
    	}
    	
    	//Converts whole numbers into decimal format if it is > 1
    	//This allows both whole numbers and decimals to be used
    	if (rate >= 1) {
            rate = rate / 100.0;
        }
    	
    	
    	int years = finallyRetired( principal,  expense,  rate, 120);
    	
    	
    	finalYearValue.setText(String.valueOf(years));
    	
    	//Sets final year to
    	if (finalYearValue.getText().contains("120"))
    	{
    		finalYearValue.setText("120 | Retirement funds will never deplete in lifetime");
    	}
    	
    	


    	// Build the series & chart
    	    List<Double> series = balanceSeries(principal, expense, rate);
    	    JFreeChart chart = buildDepletionJFreeChart(series);
    	    ChartPanel chartPanel = new ChartPanel(chart);

    	    // Large preferred size so scroll bars are useful
    	    chartPanel.setPreferredSize(new Dimension(700, 400));
    	    chartPanel.setMouseWheelEnabled(true);
    	    chartPanel.setDomainZoomable(true);
    	    chartPanel.setRangeZoomable(true);

    	    // Update ONLY the scroll pane viewport; do NOT remove rightExpen
    	    
    	     expenseScrollPane.setViewportView(chartPanel);
    	     expenseScrollPane.revalidate();  // revalidate the scroll pane
    	     expenseScrollPane.repaint();
    



    });
    
    backToMenuE.addActionListener(e->{
    	clearEBtn.doClick();
    	cardLayout.show(mainPanel, "Menu");
    });
    
    clearEBtn.addActionListener(e ->{
    	balanceField.setText("");
    	expenseField.setText("");
    	rateField.setText("");
    	finalYearValue.setText("");
    	
    	
    	JPanel placeholder = new JPanel(new GridBagLayout());
	    JLabel msg = new JLabel("No chart to display. Enter values and click Calculate.",
	                            SwingConstants.CENTER);
	    msg.setFont(new Font("Arial", Font.ITALIC, 14));
	    placeholder.add(msg, new GridBagConstraints());
	    expenseScrollPane.setViewportView(placeholder);
	
	    // 4) Refresh UI
	    rightExpen.revalidate();
	    rightExpen.repaint();
    });
    
    //Initializes left and right Components of Optimization Withdrawal Page
    JLabel titleO = new JLabel("Optimization Withdrawal", SwingConstants.CENTER);
    titleO.setFont(new Font("Arial", Font.BOLD, 20));
    JLabel calcuO = new JLabel("Calculator", SwingConstants.CENTER);
    calcuO.setFont(new Font("Arial", Font.BOLD, 20));
    
    JLabel initialBalLabel = new JLabel("Balance");
    JTextField initialBalField = new JTextField(15);
    
    JLabel returnRateLabel = new JLabel("Expected Rate");
    JTextField returnRateField = new JTextField(15);
    
    JLabel targetYearLabel = new JLabel("Target Years");
    JTextField targetYearField = new JTextField(15);
    
    JLabel percentageReturn = new JLabel("%");
    percentageReturn.setFont(new Font("Arial", Font.BOLD, 13));

    
    JButton calculateOp = new JButton("Calculate");
    calculateOp.setBackground(new Color(0, 100, 0));
    calculateOp.setForeground(Color.white);
		
	JButton clearOpBtn = new JButton("Clear");
	clearOpBtn.setBackground(Color.DARK_GRAY);
	clearOpBtn.setForeground(Color.white);
		
	JButton backToMenuOp = new JButton("Back to Menu");
	backToMenuOp.setBackground(new Color(0, 0, 139));
	backToMenuOp.setForeground(Color.white);

	JLabel resultsOp = new JLabel("Results");
	resultsOp.setFont(new Font("Arial", Font.BOLD, 20));
	
	JLabel annualWithdrawlLabel = new JLabel("Optimization Withdrawal:");
	annualWithdrawlLabel.setFont(new Font("Arial", Font.BOLD, 13));
	
	JLabel annualWithdrawlValue = new JLabel("$");
	annualWithdrawlValue.setFont(new Font("Arial", Font.BOLD, 15));
    
    //Uses addToGridBag method to add components to left side of Optimization Withdrawal Page

    GridBagConstraints gc8 = new GridBagConstraints();
    gc8.insets = new Insets(10, 10, 10, 10);
    gc8.fill = GridBagConstraints.HORIZONTAL;
    
    addToGridBag(leftOp,titleO,gc8,0,1,3,1);
    addToGridBag(leftOp,calcuO,gc8,0,2,3,1);
    
    addToGridBag(leftOp,initialBalLabel,gc8,0,3,1,1);
    addToGridBag(leftOp,initialBalField,gc8,1,3,1,1);
    
    addToGridBag(leftOp,returnRateLabel,gc8,0,4,1,1);
    addToGridBag(leftOp,returnRateField,gc8,1,4,1,1);
    addToGridBag(leftOp,percentageReturn,gc8,2,4,1,1);

    addToGridBag(leftOp,targetYearLabel,gc8,0,5,1,1);
    addToGridBag(leftOp,targetYearField,gc8,1,5,1,1);

    addToGridBag(leftOp,calculateOp,gc8,1,6,1,1);
    addToGridBag(leftOp,clearOpBtn,gc8,1,7,1,1);
    addToGridBag(leftOp,backToMenuOp,gc8,1,8,1,1);
    
    //Uses addToGridBag method to add components to right side of Optimization Withdrawal Page

    
    GridBagConstraints gc9 = new GridBagConstraints();
    gc9.insets = new Insets(10, 10, 10, 10);
    gc9.fill = GridBagConstraints.HORIZONTAL;
    
    addToGridBag(rightOp,resultsOp,gc9,0,1,1,1);
    addToGridBag(rightOp,annualWithdrawlLabel,gc9,0,2,1,1);
    addToGridBag(rightOp,annualWithdrawlValue,gc9,1,2,1,1);
    
    gc9.gridx = 0;
	gc9.gridy = 3;
	gc9.gridwidth = 2;            // span both input columns
	gc9.gridheight = 2;           // let it occupy multiple rows
	gc9.insets = new Insets(10, 10, 10, 10);
	gc9.weightx = 1.0;            // allow horizontal growth
	gc9.weighty = 1.0;            // allow vertical growth
	gc9.fill = GridBagConstraints.BOTH;

 

	rightOp.add(optimizeScrollPane, gc9);

    //Back to Home Page from the Optimization Withdrawal Menu
    backToMenuOp.addActionListener(e->{
    	clearOpBtn.doClick();
    	cardLayout.show(mainPanel,"Menu");
    });
    
    
    //Clear contents of Optimization Withdrawal Page
    clearOpBtn.addActionListener(e ->{
    	
    	initialBalField.setText("");
    	returnRateField.setText("");
    	targetYearField.setText("");
    	annualWithdrawlValue.setText("$");
    	
    	JPanel placeholder = new JPanel(new GridBagLayout());
	    JLabel msg = new JLabel("No chart to display. Enter values and click Calculate.",
	                            SwingConstants.CENTER);
	    msg.setFont(new Font("Arial", Font.ITALIC, 14));
	    placeholder.add(msg, new GridBagConstraints());
	    optimizeScrollPane.setViewportView(placeholder);
	
	    // 4) Refresh UI
	    rightOp.revalidate();
	    rightOp.repaint();
    	
    });
    
    calculateOp.addActionListener(e->{
    	//Validation Check to see if one or more fields are empty.
    	//This prevents calculation if this is met
    	if(initialBalField.getText().compareTo("") ==0 || returnRateField.getText().compareTo("") == 0 || targetYearField.getText().compareTo("") == 0)
    	{
    		JOptionPane.showMessageDialog(retireExpenPage, "One or more fields are empty", "Warning", JOptionPane.WARNING_MESSAGE);
    		return;
    	}
    	
    	//Validation check to see if one or more fields are in the data type
    	//Throws Exception and prevents calculation if this is met
    	try
    	{
    		 balance = Double.parseDouble(initialBalField.getText());
        	 rate = Double.parseDouble(returnRateField.getText());
        	 years  = Integer.parseInt(targetYearField.getText());
        	 
    	}catch(NumberFormatException nf)
    	{
    		JOptionPane.showMessageDialog(optimizePage, "Invalid datatype. All fields must be in number format.", "Warning", JOptionPane.WARNING_MESSAGE);
    		return;
    	}
    	
    	//Validation check for balance, rate, years to see if they are non-negative numbers
    	if(balance <= 0)
    	{
    		JOptionPane.showMessageDialog(optimizePage, "Investment must be a postive number (numbers greater than 0)", "Warning", JOptionPane.WARNING_MESSAGE);
    		return;
    	}
    	
    	if(rate <= 0)
    	{
    		JOptionPane.showMessageDialog(optimizePage, "Return Rate must be a postive number (numbers greater than 0)", "Warning", JOptionPane.WARNING_MESSAGE);
    		return;
    	}
    	
    	if (rate > 1000) 
    	{ 
			
    		JOptionPane.showMessageDialog(optimizePage, "Rate is too large. Please use 1000 or less.", "Warning", JOptionPane.ERROR_MESSAGE);
		   
		    System.err.println("Rate is value too large. Please use 1000 or less.");
    		return;
    	}
    	
    	
    	if(years <= 0)
    	{
    		JOptionPane.showMessageDialog(optimizePage, "Target Years must be a postive number (numbers greater than 0)", "Warning", JOptionPane.WARNING_MESSAGE);
    		return;
    	}
    	

		if (years > 1000) { // or any sensible limit
			
    		JOptionPane.showMessageDialog(optimizePage, "Target Years is too large. Please use 1000 or less.", "Warning", JOptionPane.ERROR_MESSAGE);
		   
		    System.err.println("Target years is value too large. Please use 1000 or less.");
    		return;

		}

    	
    	//Converts whole numbers into decimal format if it is > 1
    	//This allows both whole numbers and decimals to be used
    	
    	if (rate >= 1) {
            rate = rate / 100.0;
        }
    	
    	 double withdrawal =  maximumExpensed(balance, rate, years);
    	 
    	 annualWithdrawlValue.setText("$" + String.format("%.2f",(withdrawal)));
    	 

    	// targetYears: from a field or combo; for example:

    	// 1) Find optimal spending via binary search
    	double optimalSpend  = maximumExpensed(balance, rate, years);

    	// 2) Build the balance history for the optimal spending

		List<Double> history = retirementHistory(balance, optimalSpend, rate, 120);
		JFreeChart chart = buildOptimalWithdrawalChart(history, optimalSpend);

    	// 3) Build the chart that matches the screenshot
    	ChartPanel chartPanel = new ChartPanel(chart);
    	chartPanel.setPreferredSize(new Dimension(700, 400));
    	chartPanel.setMouseWheelEnabled(true);
    	chartPanel.setDomainZoomable(true);
    	chartPanel.setRangeZoomable(true);

    	// 4) Update ONLY the scroll pane’s viewport (do not remove right panel or the finalYear label)
    	    optimizeScrollPane.setViewportView(chartPanel);
    	    optimizeScrollPane.revalidate();
    	    optimizeScrollPane.repaint();
    	 
    	
    });
     
		this.setVisible(true);	
	
	}
	//end Main() 
	
	
	//This methods builds the fixed investor chart
	public static JFreeChart buildFixedInvestmentChart(double principal, double rate, int years) {
	    // Convert percentage to decimal if needed (e.g., 80 -> 0.80)
	    if (rate >= 1.0) {
	        rate = rate / 100.0;
	    }

	    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
	    double balance = principal;

	    // Populate dataset by simulating growth per year
	    for (int year = 1; year <= years; year++) {
	        balance = fixedInvestor(balance, rate, 1); // assuming 1 year per step
	        dataset.addValue(balance, "Investment Growth", String.valueOf(year));
	    }

	    // Create a category line chart
	    JFreeChart chart = ChartFactory.createLineChart(
	            "Investment Growth Over Time",
	            "Year",
	            "Balance ($)",
	            dataset
	    );

	    // Optional: light styling and currency axis format
	    CategoryPlot plot = chart.getCategoryPlot();
	    plot.setBackgroundPaint(java.awt.Color.WHITE);
	    plot.setDomainGridlinesVisible(true);
	    plot.setDomainGridlinePaint(new java.awt.Color(210, 210, 210));
	    plot.setRangeGridlinePaint(new java.awt.Color(210, 210, 210));

	    // Currency formatting on the range (Y) axis
	    org.jfree.chart.axis.NumberAxis rangeAxis = (org.jfree.chart.axis.NumberAxis) plot.getRangeAxis();
	    rangeAxis.setNumberFormatOverride(NumberFormat.getCurrencyInstance(Locale.getDefault()));

	    return chart; // <-- return JFreeChart
	}
	
	//End of buildFixedInvestmentChart method
	
	//This methods builds variable investor chart
	private ChartPanel buildVariableXYChartFromRates(double principal, double[] rateList) {
	    XYSeries balanceSeries = new XYSeries("Balance");
	    XYSeries zeroSeries = new XYSeries("Zero");

	    double balance = principal;
	    for (int i = 0; i < rateList.length; i++) {
	        balance *= (1.0 + rateList[i]);
	        int year = i + 1;
	        balanceSeries.add(year, balance);
	        zeroSeries.add(year, 0.0);
	    }

	    XYSeriesCollection dataset = new XYSeriesCollection();
	    dataset.addSeries(balanceSeries);
	    dataset.addSeries(zeroSeries);

	    JFreeChart chart = ChartFactory.createXYLineChart(
	        "Variable Rate Growth",
	        "Years",
	        "Account Balance ($)",
	        dataset
	    );

	    XYPlot plot = chart.getXYPlot();

	    NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
	    rangeAxis.setNumberFormatOverride(java.text.NumberFormat.getCurrencyInstance());
	    rangeAxis.setAutoRangeIncludesZero(false);

	    NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
	    domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
	    domainAxis.setLowerMargin(0.05);
	    domainAxis.setUpperMargin(0.05);

	    plot.setBackgroundPaint(Color.WHITE);
	    plot.setDomainGridlinesVisible(true);
	    plot.setRangeGridlinesVisible(true);
	    plot.setDomainGridlinePaint(new Color(200, 200, 200));
	    plot.setRangeGridlinePaint(new Color(200, 200, 200));

	    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
	    renderer.setSeriesPaint(0, new Color(27, 94, 32)); // balance series dark green
	    renderer.setSeriesStroke(0, new BasicStroke(2f));
	    renderer.setSeriesShape(0, new Ellipse2D.Double(-4, -4, 8, 8));     // circle markers

	    renderer.setSeriesPaint(1, new Color(198, 40, 40));                 // red
	    renderer.setSeriesStroke(1, new BasicStroke(1.2f));
	    renderer.setSeriesShapesVisible(1, false);

	    plot.setRenderer(renderer);

	    ChartPanel chartPanel = new ChartPanel(chart);
	    chartPanel.setPreferredSize(new Dimension(700, 400)); // large -> scrollbars appear
	    chartPanel.setMouseWheelEnabled(true);
	    chartPanel.setDomainZoomable(true);
	    chartPanel.setRangeZoomable(true);

	    return chartPanel;
	}
	
	//end of buildVariableXYChartFromRates method
	
//Helper functions of variable investor chart
	private void highlightInvalidRows(JTable table, java.util.List<Integer> rows) {
	    invalidRowSet.clear();
	    invalidRowSet.addAll(rows);

	    table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
	        /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
	        public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
	                                                       boolean hasFocus, int row, int column) {
	            Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
	            if (invalidRowSet.contains(row)) {
	                c.setBackground(new Color(255, 235, 238)); // light red
	            } else {
	                c.setBackground(isSelected ? tbl.getSelectionBackground() : Color.WHITE);
	            }
	            return c;
	        }
	    });

	    table.repaint();
	}

	private void clearHighlight(JTable table) {
	    invalidRowSet.clear();
	    // Reset renderer to default
	    table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer());
	    table.repaint();
	}
	
	
	private void commitTableEdits(JTable table) {
	    if (table.isEditing()) {
	        TableCellEditor editor = table.getCellEditor();
	        if (editor != null) {
	            editor.stopCellEditing(); // commits editor value into the model
	        }
	    }
	}

//This method builds retirement depletion chart

public static JFreeChart buildDepletionJFreeChart(List<Double> balances) {
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();

    // Populate dataset using the balances list
    for (int i = 0; i < balances.size(); i++) {
        int year = i + 1; // Year starts at 1
        dataset.addValue(balances.get(i), "Investment Growth", String.valueOf(year));
    }

    // Create a category line chart
    JFreeChart chart = ChartFactory.createLineChart(
            "Investment Growth Over Time",
            "Year",
            "Balance ($)",
            dataset
    );

    // Styling
    CategoryPlot plot = chart.getCategoryPlot();
    plot.setBackgroundPaint(java.awt.Color.WHITE);
    plot.setDomainGridlinesVisible(true);
    plot.setDomainGridlinePaint(new java.awt.Color(210, 210, 210));
    plot.setRangeGridlinePaint(new java.awt.Color(210, 210, 210));

    // Currency formatting on Y-axis
    org.jfree.chart.axis.NumberAxis rangeAxis = (org.jfree.chart.axis.NumberAxis) plot.getRangeAxis();
    rangeAxis.setNumberFormatOverride(NumberFormat.getCurrencyInstance(Locale.getDefault()));

    return chart;
}



//helper function of retirement depletion chart
public static List<Double> balanceSeries(double balance, double expense, double rate) {
    List<Double> series = new ArrayList<>();
    series.add(balance);               // Year 0 starting balance
    int years = 0;
    int MAX_YEARS = 2000;              // safety cap for runaway scenarios

    while (balance > 0 && years < MAX_YEARS) {
        double interest = balance * rate;         // annual growth
        balance = balance + interest - expense;   // net after expense
        series.add(Math.max(balance, 0));         // clamp at 0 for last point
        years++;
    }
    return series;
}	


//This method builds Optimal Withdrawal chart
public static JFreeChart buildOptimalWithdrawalChart(List<Double> history, double optimalSpend) {
    // Series: account balance each year (Year 0 .. Year N)
    XYSeries balanceSeries = new XYSeries("Balance");
    for (int year = 0; year < history.size(); year++) {
        balanceSeries.add(year, history.get(year));
    }

    // Series: zero baseline across the same horizon
    XYSeries zeroSeries = new XYSeries("Zero");
    for (int year = 0; year < history.size(); year++) {
        zeroSeries.add(year, 0.0);
    }

    XYSeriesCollection dataset = new XYSeriesCollection();
    dataset.addSeries(balanceSeries);
    dataset.addSeries(zeroSeries);

    // Title with formatted withdrawal (locale-aware)
    NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.getDefault());
    String title = String.format("AofA Financial Simulation: Optimal Withdrawal (%s/yr)",
            currency.format(optimalSpend));

    JFreeChart chart = ChartFactory.createXYLineChart(
            title,
            "Years",
            "Account Balance ($)",
            dataset
    );

    XYPlot plot = chart.getXYPlot();

    // Renderer: series 0 (balance) as green line with circular markers
    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
    renderer.setSeriesPaint(0, new Color(27, 94, 32));                  // dark green
    renderer.setSeriesStroke(0, new java.awt.BasicStroke(2.0f));
    renderer.setSeriesShape(0, new Ellipse2D.Double(-4, -4, 8, 8));     // circle markers

    // Renderer: series 1 (zero baseline) as thin red line, no markers
    renderer.setSeriesPaint(1, new Color(198, 40, 40));                 // red
    renderer.setSeriesStroke(1, new java.awt.BasicStroke(1.5f));
    renderer.setSeriesShapesVisible(1, false);

    plot.setRenderer(renderer);

    // Axis formatting: currency on Y
    NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
    yAxis.setNumberFormatOverride(currency);
    yAxis.setAutoRangeIncludesZero(true);

    // Visual polish
    plot.setBackgroundPaint(Color.WHITE);
    plot.setDomainGridlinePaint(new Color(210, 210, 210));
    plot.setRangeGridlinePaint(new Color(210, 210, 210));
    plot.setAxisOffset(new RectangleInsets(5, 5, 5, 5));

    return chart;
}

//end of buildOptimalWithdrawalChart method

//helper function of optimal withdrawal chart
public static List<Double> retirementHistory(double balance, double annualExpense, double rate, int capYears) {
    List<Double> history = new ArrayList<>();
    history.add(balance); // Year 0
    int years = 0;
    double B = balance;
    while (B > 0 && years < capYears) {
        B -= annualExpense;          // withdraw first
        if (B > 0) B *= (1 + rate);  // grow remaining
        else B = 0;
        history.add(B);
        years++;
    }
    return history;
}



//This allows components to be added to panel at a certain position
public void addToGridBag(JPanel panel, Component component, GridBagConstraints gbc, int column, int row,  int colspan, int rowspan)
	{

		if (panel == null ) {
		        System.out.println("addToGridBag - Panel is null!");
		        return;
		    }
		
		if(component == null)
		{
			System.out.println("addToGridBag - Component is null!");
	        return;
		}
			
	
	
	        gbc.gridx = column;
	        gbc.gridy = row;
	
	        gbc.gridwidth = colspan;
	        gbc.gridheight = rowspan;
	        
	        
	        panel.add(component,gbc);
   }


	
	public static void main(String[] args)
	{
		//Creates an anonymous object of MainWindow() 
		//This method helps to run and load up the GUI
		new MainWindow();
	}
		

}
