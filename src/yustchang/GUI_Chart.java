/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yustchang;

import java.awt.Color;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;


//import org.jfree.ui.Spacer;

/**
 * A demonstration application showing a time series chart where you can dynamically add
 * (random) data by clicking on a button.
 *
 */
public class GUI_Chart extends GUI_View{

    
    private GUI_Model model;
    private GUI_View view;
    
    private TimeSeries series1;
    private TimeSeries series2;
    private TimeSeries series3;
    private TimeSeries series4;

    
            
       
    /** The number of subplots. */
    int SUBPLOT_COUNT= 4;
    
    /** The datasets. */
    public TimeSeriesCollection[] datasets;
    
    /** The most recent value added to series 1. */
    public double[] lastValue = new double[SUBPLOT_COUNT];

    public GUI_Chart(GUI_Model model) {
        super(model);
            
    }

 
    public void GUI_Chart() {
   
        
//        super(title);
        
        final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new DateAxis("Time"));
        this.datasets = new TimeSeriesCollection[SUBPLOT_COUNT];
        
        
            final NumberAxis rangeAxis = new NumberAxis("Y" + 0);
            rangeAxis.setAutoRangeIncludesZero(false);
        
            series1 = new TimeSeries("Random " + 0, Millisecond.class);
            this.datasets[0] = new TimeSeriesCollection(series1);
            series2 = new TimeSeries("Random " + 1, Millisecond.class);
            this.datasets[1] = new TimeSeriesCollection(series2);      
            series3 = new TimeSeries("Random " + 2, Millisecond.class);
            this.datasets[2] = new TimeSeriesCollection(series3);
            series4 = new TimeSeries("Random " + 2, Millisecond.class);
            this.datasets[3] = new TimeSeriesCollection(series4);

            
            final XYPlot subplot = new XYPlot(
                    this.datasets[0], null, rangeAxis, new StandardXYItemRenderer()
            );
            subplot.setDataset(1,datasets[1]);
            
            //subplot.mapDatasetToDomainAxis(1, 0); // same axis, different dataset
            //subplot.mapDatasetToRangeAxis(1, 0); // same axis, different dataset

            // for figure 1 and 2
            final XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
            renderer.setSeriesPaint(0, Color.BLUE);
            subplot.setRenderer(1, renderer);
            
            subplot.setBackgroundPaint(Color.white);
            subplot.setDomainGridlinePaint(Color.GRAY);
            subplot.setRangeGridlinePaint(Color.GRAY);
            plot.add(subplot);


            // for figure 3
            final NumberAxis rangeAxis2 = new NumberAxis("Y" + 2);
            rangeAxis2.setAutoRangeIncludesZero(false);
            final XYPlot subplot2 = new XYPlot(
                    this.datasets[2], null, rangeAxis2, new StandardXYItemRenderer()
            );
            subplot2.setBackgroundPaint(Color.white);
            subplot2.setDomainGridlinePaint(Color.GRAY);
            subplot2.setRangeGridlinePaint(Color.GRAY);
            plot.add(subplot2);

            // for figure 4
            final NumberAxis rangeAxis3 = new NumberAxis("Y" + 2);
            rangeAxis2.setAutoRangeIncludesZero(false);
            final XYPlot subplot3 = new XYPlot(
                    this.datasets[3], null, rangeAxis2, new StandardXYItemRenderer()
            );
            subplot3.setBackgroundPaint(Color.white);
            subplot3.setDomainGridlinePaint(Color.GRAY);
            subplot3.setRangeGridlinePaint(Color.GRAY);
            plot.add(subplot3);
        
        
        // for the figure
        final JFreeChart chart = new JFreeChart("Temperature moniter", plot);
//        chart.getLegend().setAnchor(Legend.EAST);
        chart.setBorderPaint(Color.black);
        chart.setBorderVisible(true);
        chart.setBackgroundPaint(Color.white);
        
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);
  //      plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 4, 4, 4, 4));
        final ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis.setFixedAutoRange(60000.0);  // 60 seconds
        

       //  final JPanel content = new JPanel(new BorderLayout());
        final ChartPanel chartPanel = new ChartPanel(chart);
        // content.add(chartPanel);   
                
       // chartPanel.setPreferredSize(new java.awt.Dimension(800, 500));
       // chartPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
       // setContentPane(content);
    }
    
    public void updateChart(double mean, double max){
    
        lastValue[0] = mean;
        lastValue[1] = max;
        datasets[0].getSeries(0).add(new Millisecond(), lastValue[0]);       
        datasets[1].getSeries(0).add(new Millisecond(), lastValue[1]);     
        
        lastValue[2] = 100 * (0.90 + 0.2 * Math.random());         
        datasets[2].getSeries(0).add(new Millisecond(), lastValue[2]);  
        
        lastValue[3] = 100 * (0.90 + 0.2 * Math.random());         
        datasets[3].getSeries(0).add(new Millisecond(), lastValue[3]);  
        
    }
    
    
    /* standalone runner 
    public void updateChart(){
        
            timer = new Timer(1000, new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    
                    final Millisecond now = new Millisecond();
                    System.out.println("Now = " + now.toString());
                    for (int i = 0; i < SUBPLOT_COUNT; i++) {
                            lastValue[i] = lastValue[i] * (0.90 + 0.2 * Math.random());
                            // lastValue[i] = lastValue[i] * (0.90 + 0.2 * Math.random());
                            datasets[i].getSeries(0).add(new Millisecond(), lastValue[i]);       
                    }
                    
                }
            });
        
            timer.start();
    }
    */
    
 
    
}