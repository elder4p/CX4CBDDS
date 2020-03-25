/**
 * Copyright 2018 ADLINK Technology Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.zeligsoft.domain.dds4ccm.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.uml2.uml.Element;

import com.zeligsoft.base.ui.utils.BaseUIUtil;
import com.zeligsoft.base.zdl.util.ZDLUtil;
import com.zeligsoft.domain.dds4ccm.DDS4CCMNames;
import com.zeligsoft.domain.dds4ccm.ui.l10n.Messages;
import com.zeligsoft.domain.dds4ccm.utils.DDS4CCMUtil;

/**
 * Manual action to convert DDSMessage to CORBAStruct with all owned attributes
 * also converted to CORBAField.
 * 
 * @author parmvirs
 * 
 */
public class ConvertFieldToStructActionHandler extends AbstractHandler {

	private int refactorCount = 0;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		refactorCount = 0;
		EObject selObject = BaseUIUtil.getEObjectFromSelection(BaseUIUtil.getSelection());

		if (selObject != null && ZDLUtil.isZDLConcept(selObject, DDS4CCMNames.DDSMESSAGE)) {

			Command command = new ConvertFieldtoStructCommand(selObject, Messages.ConvertFieldToStruct_DialogTitle);
			if (command.canExecute()) {
				TransactionUtil.getEditingDomain(selObject).getCommandStack().execute(command);
			}
		}

		if (refactorCount > 0) {
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
					Messages.ConvertFieldToStruct_DialogTitle, Messages.Migrate_OK);
		} else {
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
					Messages.ConvertFieldToStruct_DialogTitle, Messages.Migrate_Noop);
		}
		
		return null;
	}

	/**
	 * Internal command used to perform the migration.
	 * 
	 * @author parmvirs
	 * 
	 */
	private class ConvertFieldtoStructCommand extends RecordingCommand {

		private EObject refactorObject = null;

		/**
		 * Constructor
		 * 
		 * @param modelToMigrate
		 * @param label
		 */
		public ConvertFieldtoStructCommand(EObject modelToMigrate, String label) {

			super(TransactionUtil.getEditingDomain(modelToMigrate), label);

			this.refactorObject = modelToMigrate;

		}

		@Override
		protected void doExecute() {

			if (ZDLUtil.isZDLConcept(refactorObject, DDS4CCMNames.DDSMESSAGE)) {
				refactorCount++;
				DDS4CCMUtil.convertDDSMessageToCORBAStruct((Element) refactorObject);
			}
		}

		@Override
		public boolean canExecute() {
			return (refactorObject != null);
		}
	}

}
