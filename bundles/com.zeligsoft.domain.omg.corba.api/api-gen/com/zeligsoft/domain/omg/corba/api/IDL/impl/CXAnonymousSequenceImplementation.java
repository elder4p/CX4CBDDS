package com.zeligsoft.domain.omg.corba.api.IDL.impl;

import com.zeligsoft.base.zdl.staticapi.internal.core.ZObjectImpl;

import com.zeligsoft.domain.omg.corba.api.IDL.CXAnonymousSequence;

public class CXAnonymousSequenceImplementation extends ZObjectImpl implements CXAnonymousSequence {
	public CXAnonymousSequenceImplementation(org.eclipse.emf.ecore.EObject element) {
		super(element);
	}

	@Override
	public org.eclipse.uml2.uml.DataType asDataType() {
		return (org.eclipse.uml2.uml.DataType) eObject();
	}
}
