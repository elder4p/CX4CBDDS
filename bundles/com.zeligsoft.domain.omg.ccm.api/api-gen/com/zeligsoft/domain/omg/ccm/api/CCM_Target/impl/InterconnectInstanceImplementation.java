package com.zeligsoft.domain.omg.ccm.api.CCM_Target.impl;

import com.zeligsoft.base.zdl.staticapi.util.ZDLFactoryRegistry;

import com.zeligsoft.domain.omg.ccm.api.CCM_Target.InterconnectInstance;
import com.zeligsoft.domain.zml.api.ZML_Core.impl.NamedElementImplementation;

import com.zeligsoft.domain.zml.api.ZML_Core.Type;
import com.zeligsoft.domain.omg.ccm.api.CCM_Target.Interconnect;

import com.zeligsoft.base.zdl.util.ZDLUtil;

public class InterconnectInstanceImplementation extends
		NamedElementImplementation implements InterconnectInstance {
	protected Interconnect _type;

	public InterconnectInstanceImplementation(
			org.eclipse.emf.ecore.EObject element) {
		super(element);
	}

	@Override
	public Integer getLowerBound() {
		final Object rawValue = com.zeligsoft.base.zdl.util.ZDLUtil.getValue(
				eObject(), "ZMLMM::ZML_Core::TypedElement", "lowerBound");
		return (Integer) rawValue;
	}

	@Override
	public void setLowerBound(Integer val) {
		ZDLUtil.setValue(element, "ZMLMM::ZML_Core::TypedElement",
				"lowerBound", val);
	}

	@Override
	public String getLabel() {
		final Object rawValue = com.zeligsoft.base.zdl.util.ZDLUtil.getValue(
				eObject(), "CCM::CCM_Target::InterconnectInstance", "label");
		return (String) rawValue;
	}

	@Override
	public void setLabel(String val) {
		ZDLUtil.setValue(element, "CCM::CCM_Target::InterconnectInstance",
				"label", val);
	}

	@Override
	public Interconnect getTypeOverride() {
		if (_type == null) {
			final Object rawValue = com.zeligsoft.base.zdl.util.ZDLUtil
					.getValue(eObject(),
							"CCM::CCM_Target::InterconnectInstance", "type");
			if (rawValue instanceof org.eclipse.emf.ecore.EObject) {
				_type = ZDLFactoryRegistry.INSTANCE.create(
						(org.eclipse.emf.ecore.EObject) rawValue,
						Interconnect.class);
			}
		}
		return _type;
	}

	@Override
	public void setTypeOverride(Interconnect val) {
		ZDLUtil.setValue(element, "CCM::CCM_Target::InterconnectInstance",
				"type", val.eObject());
	}

	@Override
	public Type getType() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setType(Type val) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Integer getUpperBound() {
		final Object rawValue = com.zeligsoft.base.zdl.util.ZDLUtil.getValue(
				eObject(), "ZMLMM::ZML_Core::TypedElement", "upperBound");
		return (Integer) rawValue;
	}

	@Override
	public void setUpperBound(Integer val) {
		ZDLUtil.setValue(element, "ZMLMM::ZML_Core::TypedElement",
				"upperBound", val);
	}

	@Override
	public org.eclipse.uml2.uml.Property asProperty() {
		return (org.eclipse.uml2.uml.Property) eObject();
	}
}
