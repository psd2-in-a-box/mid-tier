package dk.sample.rest.bank.virtualaccount.exposure.rs.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.UriInfo;

import dk.nykredit.jackson.dataformat.hal.HALLink;
import dk.nykredit.jackson.dataformat.hal.annotation.EmbeddedResource;
import dk.nykredit.jackson.dataformat.hal.annotation.Link;
import dk.nykredit.jackson.dataformat.hal.annotation.Resource;
import dk.sample.rest.bank.virtualaccount.exposure.rs.MicroPlanServiceExposure;
import dk.sample.rest.bank.virtualaccount.model.MicroPlan;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 *
 */
@Resource
@ApiModel(value = "Microplans ",
        description = "a list of Micro Plans")

public class MicroPlansRepresentation {

    @Link
    private HALLink self;

    @EmbeddedResource("microplans")
    private Collection<MicroPlanRepresentation> plans;

    public MicroPlansRepresentation(List<MicroPlan> plans, UriInfo uriInfo) {
        this.plans = new ArrayList<>();
        this.plans.addAll(plans.stream().map(plan -> new MicroPlanRepresentation(plan, uriInfo))
                .collect(Collectors.toList()));
        this.self = new HALLink.Builder(uriInfo.getBaseUriBuilder()
                .path(MicroPlanServiceExposure.class)
                .build())
                .build();
    }

    @ApiModelProperty(
            access = "public",
            name = "self",
            notes = "link to the list of plans.")
    public HALLink getSelf() {
        return self;
    }

    @ApiModelProperty(
            access = "public",
            name = "microplans",
            value = "micro plan list.")
    public Collection<MicroPlanRepresentation> getPlans() {
        return Collections.unmodifiableCollection(plans);
    }

}
