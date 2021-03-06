package cern.laser.definition;

import java.io.Reader;
import java.io.Writer;
import java.util.Collection;

import cern.laser.business.definition.data.CategoryDefinition;


/** Provides the service to handle category definitions. Category definitions are
 * organized in trees with a common root. A user can be responsible for
 * one ore more subtrees and a subtree can be administered by one ore more user.
 * @see cern.laser.definition.AdminUser
 */
public interface CategoryDefinitionHandler {
  /** Create a new category definition.
   * @param definition the category definition
   * @throws LaserDefinitionNotValidException if the definition failed validation
   * @throws LaserDefinitionNotAllowedException if the user is not allowed
   * @throws LaserDefinitionException if the request can not be served
   */
  public void createCategory(CategoryDefinition definition) throws LaserDefinitionException;

  /** Dump the user definitions in XML format.
   * @param xmlDefinitionsWriter the XML definitions writer
   * @throws LaserDefinitionXMLException if the XML marshalling failed
   * @throws LaserDefinitionException if the request can not be served
   */
  public void download(Writer xmlDefinitionsWriter) throws LaserDefinitionException;

  /** Remove an category definition by its identifier.
   * @param definition the category definition
   * @throws LaserDefinitionNotFoundException if the category definition was not found
   * @throws LaserDefinitionNotAllowedException if the user is not allowed
   * @throws LaserDefinitionException if the request can not be served
   */
  public void removeCategory(CategoryDefinition definition) throws LaserDefinitionException;

  /** Update a category definition.
   * @param definition the new category definition
   * @throws LaserDefinitionNotFoundException if the category definition was not found
   * @throws LaserDefinitionNotValidException if the definition failed validation
   * @throws LaserDefinitionNotAllowedException if the user is not allowed
   * @throws LaserDefinitionException if the request can not be served
   */
  public void updateCategory(CategoryDefinition definition) throws LaserDefinitionException;

  /** Execute a bulk update within one single transaction.
   * @param toBeCreated the definitions to create
   * @param toBeUpdated the definitions to update
   * @param toBeRemoved the definitions to remove
   * @throws LaserDefinitionNotValidException if the definition failed validation
   * @throws LaserDefinitionNotFoundException if the definition was not found
   * @throws LaserDefinitionNotAllowedException if the user is not allowed
   * @throws LaserDefinitionException if the request can not be served
   */
  public void upload(Collection toBeCreated, Collection toBeUpdated, Collection toBeRemoved) throws LaserDefinitionException;

  /** Execute a bulk update within one single transaction.
   * @param xmlDefinitionsReader the XML definitions reader
   * @throws LaserDefinitionXMLException if the XML unmarshalling failed
   * @throws LaserDefinitionNotValidException if the definition failed validation
   * @throws LaserDefinitionNotFoundException if the definition was not found
   * @throws LaserDefinitionNotAllowedException if the user is not allowed
   * @throws LaserDefinitionException if the request can not be served
   */
  public void upload(Reader xmlDefinitionsReader) throws LaserDefinitionException;
}
