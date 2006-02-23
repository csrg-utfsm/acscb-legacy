/*
 * $Id: CategoryDAO.java,v 1.2 2005/04/18 08:48:50 kzagar Exp $
 *
 * $Date: 2005/04/18 08:48:50 $ 
 * $Revision: 1.2 $ 
 * $Author: kzagar $
 *
 * Copyright CERN, All Rights Reserved.
 */
package cern.laser.business.dao;

import cern.laser.business.data.Category;

/**
 * 
 * 
 * @version $Revision: 1.2 $ $Date: 2005/04/18 08:48:50 $
 * @author Katarina Sigerud
 */
public interface CategoryDAO {
  //public void setCategoryTreeRoot(String categoryTreeRoot);
  
  //public void setSurveillanceCategoryPath(String surveillanceCategoryPath);
  
  public Category findCategory(Integer identifier);
  
  //public Category findCategoryAlarmsInitialized(Integer identifier);
  
  //public Category findCategoryByPathInitialized(String path);
  
  public Category getCategory(Integer identifier);
  
  public Category findByCategoryTreeRoot();

  public Category findBySurveillanceCategory();
  
  //public Category findBySurveillanceCategoryInitialized();
  
  public Category[] findAllCategories();

  public Category findCategoryByPath(String path);
  
  public Category getCategoryByPathInitialized(String path);
  
  public Category getCategoryByPath(String path);
  
  public void saveCategory(Category category);
  
  public void updateCategory(Category category);
  
  public void deleteCategory(Category category);
  
  public String[] getAlarms(Integer categoryId);
  
  public Integer[] getChildren(Integer parentId);
  
  //public boolean hasAlarmsForSource(Integer categoryId, String sourceId);
  
  public void flushCategory();
}