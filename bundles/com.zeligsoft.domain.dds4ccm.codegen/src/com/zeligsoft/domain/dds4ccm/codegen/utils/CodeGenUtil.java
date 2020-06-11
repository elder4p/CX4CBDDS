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

package com.zeligsoft.domain.dds4ccm.codegen.utils;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.papyrus.infra.core.resource.ModelMultiException;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.infra.core.services.ExtensionServicesRegistry;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.core.services.ServiceMultiException;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;
import org.eclipse.uml2.common.util.UML2Util;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.UMLPackage;
import org.osgi.framework.Bundle;

import com.zeligsoft.base.util.WorkflowUtil;
import com.zeligsoft.cx.build.factory.ProjectFactory;
import com.zeligsoft.cx.codegen.CodeGenWorkflowConstants;
import com.zeligsoft.domain.dds4ccm.codegen.Activator;
import com.zeligsoft.domain.dds4ccm.codegen.l10n.Messages;
import com.zeligsoft.domain.dds4ccm.utils.DDS4CCMDiagnostician;
import com.zeligsoft.domain.dds4ccm.utils.DDS4CCMGenerationUtils;
import com.zeligsoft.domain.dds4ccm.utils.DDS4CCMGenerationUtils.DDS4CCMGenerationListener;
import com.zeligsoft.domain.dds4ccm.utils.DDS4CCMUtil;
import com.zeligsoft.domain.dds4ccm.utils.ICodeGenEvent;

/**
 * Code generation utility APIs
 * 
 * @author ysroh
 * 
 */
public class CodeGenUtil implements DDS4CCMGenerationListener {

	private static final String IDL_GENERATION_PLUGIN_ID = "com.zeligsoft.domain.ngc.ccm.generator"; //$NON-NLS-1$
	private static final String IDL_GENERATION_WORKFLOW_PATH = "/workflow/ngc_idl.oaw"; //$NON-NLS-1$
	private static final String DESCRIPTOR_GENERATION_PLUGIN_ID = "com.zeligsoft.domain.ngc.ccm.descriptorgeneration"; //$NON-NLS-1$
	private static final String DESCRIPTOR_GENERATION_WORKFLOW_PATH = "/workflow/bulkDescriptorsGenerator.oaw"; //$NON-NLS-1$

	private static ResourceSet rset = new ResourceSetImpl();

	private static Set<CodeGenListener> listeners = new HashSet<CodeGenListener>();

	public static CodeGenUtil INSTANCE = new CodeGenUtil();

	private CodeGenUtil() {
		DDS4CCMGenerationUtils.addGenerationListener(this);
	}

	/**
	 * Add listener to be notified for generation activity. Users must implement
	 * {@link CodeGenListener#artifactsGenerated(List)} method to handle post
	 * generation activity.
	 * 
	 * @param listener
	 */
	public void addCodeGenListener(CodeGenListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove listener from the set.
	 * 
	 * @param listener
	 */
	public void removeCodeGenListener(CodeGenListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Generated IDL
	 * 
	 * @param uri      Model resource {@link URI} <i>e.g.,
	 *                 platform:/resource/DDS/DDS4CCMModel/BasicPubSub_asm.emx</i>
	 * @param validate Validate the model before generation if set to <tt>true</tt>.
	 *                 Generation will be cancelled if status severity is
	 *                 {@link IStatus#ERROR}
	 * @return
	 * 
	 */
	public IStatus generateIDL(URI uri, boolean validate) {
		if (validate) {
			IStatus status = validateModel(uri);
			if (status.getSeverity() == IStatus.ERROR) {
				return status;
			}
		}
		return doTransform(IDL_GENERATION_PLUGIN_ID, IDL_GENERATION_WORKFLOW_PATH, uri);
	}

	public IStatus generateIDL(URI uri) {
		return generateIDL(uri, false);
	}

	/**
	 * Generated descriptors
	 * 
	 * @param uri      Model resource {@link URI} <i>e.g.,
	 *                 platform:/resource/DDS/DDS4CCMModel/BasicPubSub_asm.emx</i>
	 * @param validate Validate the model before generation if set to <tt>true</tt>.
	 *                 Generation will be cancelled if status severity is
	 *                 {@link IStatus#ERROR}
	 */
	public IStatus generateDescriptors(URI uri, boolean validate) {
		if (validate) {
			IStatus status = validateModel(uri);
			if (status.getSeverity() == IStatus.ERROR) {
				return status;
			}
		}
		return doTransform(DESCRIPTOR_GENERATION_PLUGIN_ID, DESCRIPTOR_GENERATION_WORKFLOW_PATH, uri);
	}

	public IStatus generateDescriptors(URI uri) {
		return generateDescriptors(uri, false);
	}

	/**
	 * Validate the given model
	 * 
	 * @param uri Model resource {@link URI} <i>e.g.,
	 *            platform:/resource/DDS/DDS4CCMModel/BasicPubSub_asm.emx</i>
	 * @return
	 */
	@SuppressWarnings("restriction")
	public IStatus validateModel(URI uri) {
		ServicesRegistry registry = null;
		// Starting the registry
		try {
			registry = new ExtensionServicesRegistry(org.eclipse.papyrus.infra.core.Activator.PLUGIN_ID);

			// have to create the model set and populate it with the DI model
			// before initializing other services that actually need the DI
			// model, such as the SashModel Manager service
			registry.startServicesByClassKeys(ModelSet.class);
		} catch (ServiceException ex) {
			// Ignore this exception: some services may not have been loaded,
			// which is probably normal at this point
		}
		try {
			ModelSet modelSet = registry.getService(ModelSet.class);
			modelSet.loadModels(uri);
			org.eclipse.uml2.uml.Package root = UML2Util.load(modelSet, uri, UMLPackage.Literals.PACKAGE);

			TransactionalEditingDomain domain = TransactionUtil.getEditingDomain(root);
			IProgressMonitor progressMonitor = new NullProgressMonitor();
			DDS4CCMDiagnostician diagnostician = new DDS4CCMDiagnostician();

			AdapterFactory adapterFactory = domain instanceof AdapterFactoryEditingDomain
					? ((AdapterFactoryEditingDomain) domain).getAdapterFactory()
					: null;
			diagnostician.initialize(adapterFactory, progressMonitor);

			BasicDiagnostic diagnostic = diagnostician.createDefaultDiagnostic(root);
			Map<Object, Object> context = diagnostician.createDefaultContext();

			boolean valid = diagnostician.validate(root, diagnostic, context);
			if (!valid) {
				return createStatus(IStatus.ERROR, "Model validation reported errors with URI: " + uri);
			}
		} catch (ServiceException e1) {
			return createStatus(IStatus.ERROR, e1.getMessage());
		} catch (ModelMultiException e) {
			return createStatus(IStatus.ERROR, e.getMessage());
		} finally {
			try {
				registry.disposeRegistry();
			} catch (ServiceMultiException e) {
				// do nothing
			}
		}

		return Status.OK_STATUS;
	}

	private IStatus doTransform(String pluginId, String workflowPath, URI uri) {
		Bundle bundle = Platform.getBundle(pluginId);
		URL workflow = bundle.getEntry(workflowPath);
		EObject element;
		Resource resource = rset.createResource(uri);
		try {
			resource.load(Collections.EMPTY_MAP);
			element = resource.getContents().get(0);
		} catch (IOException e) {
			return createStatus(IStatus.ERROR, e.getMessage());
		}
		Map<String, String> genProperties = new HashMap<String, String>();

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		String location = root.getLocation().toOSString();
		genProperties.put(CodeGenWorkflowConstants.PLATFORM_URI, location);

		Resource res = element.eResource();
		genProperties.put(CodeGenWorkflowConstants.MODEL_URI_STRING, res.getURI().toString());
		// where we want to look for a build environment
		if (element instanceof NamedElement) {
			genProperties.put(CodeGenWorkflowConstants.BUILD_ELEMENT, ((NamedElement) element).getQualifiedName());
		}

		HashMap<String, Object> externalSlotContents = new HashMap<String, Object>();
		externalSlotContents.put(CodeGenWorkflowConstants.ELEMENT_STRING, element);

		IProject project = ProjectFactory.getProject(element, null, ProjectFactory.MODE_CREATE_BASIC);
		if (project == null) {
			return createStatus(IStatus.ERROR, Messages.CodeGenUtil_0);
		}
		genProperties.put(CodeGenWorkflowConstants.GENERATED_PROJECT, project.getName());

		String srcGen = project.getLocation().makeAbsolute().toOSString();
		genProperties.put(CodeGenWorkflowConstants.SRC_GEN, srcGen);

		IStatus result;
		try {
			result = WorkflowUtil.executeWorkflow(workflow, null, genProperties, externalSlotContents);

		} finally {
			try {
				project.refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException e) {
				Activator.getDefault().error(e.getMessage(), e);
			}
		}
		resource.unload();
		return result;
	}

	private Status createStatus(int severity, String msg) {
		return DDS4CCMUtil.createStatus(Activator.PLUGIN_ID, severity, msg);
	}

	@Override
	public void fireArtifactGeneratedEvent(ICodeGenEvent event) {
		for (CodeGenListener l : listeners) {
			l.artifactsGenerated(event);
		}
	}

	@Override
	public void fireShowSourceEvent(ICodeGenEvent event) {
		for (CodeGenListener l : listeners) {
			l.showSource(event);
		}
	}
}
