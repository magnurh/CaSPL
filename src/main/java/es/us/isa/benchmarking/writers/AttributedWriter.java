/**
 * 	This file is part of FaMaTS.
 *
 *     FaMaTS is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     FaMaTS is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with FaMaTS.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.us.isa.benchmarking.writers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import es.us.isa.FAMA.models.FAMAAttributedfeatureModel.AttributedFeature;
import es.us.isa.FAMA.models.FAMAAttributedfeatureModel.FAMAAttributedFeatureModel;
import es.us.isa.FAMA.models.FAMAAttributedfeatureModel.Relation;
import es.us.isa.FAMA.models.FAMAfeatureModel.Feature;
import es.us.isa.FAMA.models.domain.Domain;
import es.us.isa.FAMA.models.domain.IntegerDomain;
import es.us.isa.FAMA.models.domain.ObjectDomain;
import es.us.isa.FAMA.models.domain.Range;
import es.us.isa.FAMA.models.domain.RangeIntegerDomain;
import es.us.isa.FAMA.models.featureModel.Cardinality;
import es.us.isa.FAMA.models.featureModel.Constraint;
import es.us.isa.FAMA.models.featureModel.extended.GenericAttribute;
import es.us.isa.FAMA.models.variabilityModel.VariabilityModel;
import es.us.isa.FAMA.models.variabilityModel.parsers.IWriter;
/**
 * 	This file is part of FaMaTS.
 *
 *     FaMaTS is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     FaMaTS is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with FaMaTS.  If not, see <http://www.gnu.org/licenses/>.
 */

public class AttributedWriter implements IWriter {

	private BufferedWriter writer = null;
	private FAMAAttributedFeatureModel fm = null;
	private Collection<String> relationshipsCol = new ArrayList<String>();
	private Collection<String> attributesCol = new ArrayList<String>();
	private Collection<String> constraintsCol = new ArrayList<String>();
	private Collection<AttributedFeature> usedFeats = new ArrayList<AttributedFeature>();

	public void writeFile(String fileName, VariabilityModel vm)
			throws Exception {

		File file = new File(fileName);
		fm = (FAMAAttributedFeatureModel) vm;

		writer = new BufferedWriter(new FileWriter(file));

		generateStringsCols();

		Iterator<String> relColIt = relationshipsCol.iterator();
		Iterator<String> attColIt = attributesCol.iterator();
		Iterator<String> consColIt = constraintsCol.iterator();

		while (relColIt.hasNext()) {
			writer.write(relColIt.next());
			writer.write("\n");// blank
		}
		writer.write("\n");// blank

		while (attColIt.hasNext()) {
			writer.write(attColIt.next());
			writer.write("\n");// blank
		}
		writer.write("\n");// blank

		while (consColIt.hasNext()) {
			String string = consColIt.next().toString();
			if(string.contains("excludes")){
				string=string.replace("excludes", "EXCLUDES");
			}
			if(string.contains("requires")){
				string=string.replace("requires", "REQUIRES");
			}
			writer.write(string);// el maquina de jesus tenia hecho el
			// recorrido inorden del AST xD
			writer.write("\n");// blank
		}
		writer.write("\n");// blank

		writer.flush();
		writer.close();

	}

	private void generateStringsCols() {

		relationshipsCol.add("%Relationships");
		attributesCol.add("%Attributes");
		constraintsCol.add("%Constraints");

		recursiveWay(fm.getRoot());
		// Ahora las constraints
		Iterator<Constraint> constIt = fm.getConstraints().iterator();
		while (constIt.hasNext()) {
			Constraint cons = constIt.next();
			if (!(cons.toString() == " ")
					|| !constraintsCol.contains(cons.toString() + ";"))
				constraintsCol.add(cons.toString() + ";");
		}

		// rel &attribs
		// las constrainsts aparte// recorrido del ast
	}

	private void recursiveWay(AttributedFeature feat) {
		// Pillamos los atributos de la feature
		usedFeats.add(feat);
		Iterator<GenericAttribute> attIt = feat.getAttributes().iterator();
		while (attIt.hasNext()) {

			GenericAttribute att = attIt.next();
			String attLine = feat.getName() + "." + att.getName() + ": ";

			// Pillamos los dominios
			Domain domain = att.getDomain();
			String domainStr = "";
			if (domain instanceof RangeIntegerDomain) {
				domainStr += "Integer";
				Iterator<Range> rangesIt = ((RangeIntegerDomain) domain)
						.getRanges().iterator();
				while (rangesIt.hasNext()) {
					Range range = rangesIt.next();
					domainStr += "[" +range.getMin() + " to " + range.getMax() + "]";
				}
//				domainStr = domainStr.substring(0, domainStr.length() - 1);
			} else if (domain instanceof IntegerDomain) {
				domainStr += "Integer [";
				Iterator<Integer> domIt = domain.getAllIntegerValues()
						.iterator();
				while (domIt.hasNext()) {
					domainStr += domIt.next().toString() + ",";
				}
				domainStr = domainStr.substring(0, domainStr.length() - 1);
				domainStr += "]";


			} else if (domain instanceof ObjectDomain) {
				ObjectDomain dom = ((ObjectDomain) domain);
				if (dom.getObjectValues().size()>0){
				domainStr += "[";

				Iterator<Object> domIt = dom
						.getObjectValues().iterator();
				while (domIt.hasNext()) {
					domainStr += domIt.next().toString() + ",";
				}
				domainStr = domainStr.substring(0, domainStr.length() - 1);
				domainStr += "]";
				}
			}
			
			
				attributesCol.add(attLine + domainStr + ","
						+ att.getDefaultValue() + "," + att.getNullValue()
						+ ";");

			
		}
		// Origens
		Iterator<Relation> relIt = feat.getRelations();
		
		if (feat.getNumberOfRelations() > 0) {
			String relsStr = feat.getName() + " : ";
			while (relIt.hasNext()) {
				Relation rel = relIt.next();
				Iterator<AttributedFeature> destIt = rel.getDestination();
				
/*				System.out.print(rel.toString());							// mrh
				if (rel.isMandatory()) System.out.println(" Mandatory");
				else if (rel.isOptional()) System.out.println(" Optional");
				else System.out.println(" Or/Alt");*/
				
				Iterator<AttributedFeature> rhs = rel.getDestination();
				int rhsSize = 0;
				while (rhs.hasNext() && rhsSize < 2){
					rhs.next();
					rhsSize++;
				}
				//System.out.println("Number of elements in right-hand-side: "+rhsSize);
				
/*				Iterator<Cardinality> cardiIter = rel.getCardinalities();
				while (cardiIter.hasNext()) {
					System.out.print(" "+cardiIter.next()+" ");
				}
				System.out.println();*/
				
				
				if (rel.isMandatory() && rhsSize < 2) {
					while (destIt.hasNext()) {
						AttributedFeature dest = destIt.next();
						relsStr += " " + dest.getName() + " ";

					}
				} else if (rel.isOptional()) {
					while (destIt.hasNext()) {
						AttributedFeature dest = destIt.next();
						relsStr += "[" + dest.getName() + "] ";

					}
				} else {

					Iterator<Cardinality> cardIt = rel.getCardinalities();
					relsStr += "[";
					
//					System.out.println("Relation cardinality: "+cardIt.toString());		// mrh

					while (cardIt.hasNext()) {
						Cardinality card = cardIt.next();
						relsStr += card.getMin() + "," + card.getMax();
						//System.out.println("Relation String: "+relsStr); 				// mrh

					}
					relsStr += "] {";
					while (destIt.hasNext()) {
						AttributedFeature dest = destIt.next();
						relsStr += dest.getName() + " ";

					}
					relsStr += "}";

				}

			}
			relationshipsCol.add(relsStr + ";");
			relIt = feat.getRelations();
			while (relIt.hasNext()) {
				Relation rel = relIt.next();

				Iterator<AttributedFeature> destIt = rel.getDestination();
				while (destIt.hasNext()) {
					AttributedFeature dest = destIt.next();

					if (!usedFeats.contains(dest)) {
						recursiveWay(dest);
						usedFeats.add(dest);
					}
				}
			}
		}

		// Hay que procesar los invariantes
		if (feat.getInvariants().size() > 0) {
			String invStr = feat.getName() + "{\n";
			Iterator<Constraint> invIt = feat.getInvariants().iterator();
			while (invIt.hasNext()) {
				Constraint cons = invIt.next();
				invStr += "\t" + cons.toString() + "\n";
			}
			constraintsCol.add(invStr + "}");
		}

	}
}