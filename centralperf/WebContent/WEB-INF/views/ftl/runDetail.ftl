<#import 'macros/layout.macro.ftl' as layout>
<#import 'macros/run/graph-panel.macro.ftl' as graph_panel>
<#import 'macros/script/script-variables.macro.ftl' as script_variable>
<#import "spring.ftl" as spring />

<script type="text/javascript">

	// Live update of a variable value
	function updateRunVariable(inputRef){
		$.ajax({
		  type: "POST",
		  url: "${rc.contextPath}/project/${run.project.id}/run/${run.id}/variables/update",
		  data: {
		  		name : $(inputRef).attr('name'),
		  		value : $(inputRef).val()
		  	}
		});
	}
</script>

<@layout.main title="Run detail" menu="runs">
    <div class="page-header">
        <div class="page-header page-title">
	        <strong><a href="${rc.contextPath}/project/${run.project.id}/detail">${run.project.name}</a> > 
	        <a href="#" id="runLabelEditable" data-name="label" data-type="text" data-url="${rc.contextPath}/run/${run.id}" data-title="Enter run name">${run.label}</a>
            (script : <a href="${rc.contextPath}/project/${run.project.id}/script/${run.scriptVersion.id}/detail">${run.scriptVersion.number}</a>)
        </div>
        <div>
            <#if run.launched><#if !run.running>Launched on<#else>Started since</#if><span id="launchDate">${run.startDate!?string}</span><#if !run.running>to <span id="endDate">${run.endDate!?string}</span></#if>(<span id="elapsed">${runDurationInSeconds!} seconds</span>)</#if>
            <#if !run.running>
                <a href="${rc.contextPath}/project/${run.project.id}/run/${run.id}/launch" class="btn btn-success">
	                <#if run.launched>
	                	<span class="glyphicon glyphicon-forward"></span><b> launch again</b>
	                <#else>
	                	<span class="glyphicon glyphicon-play"></span><b> launch</b>
	                </#if>
                </a>
            </#if>
        </div>          
    </div>
    <div style="clear:both">
    	<legend>Comment</legend>
    		<a href="#" id="runCommentEditable" data-name="comment" data-type="textarea" data-url="${rc.contextPath}/run/${run.id}" data-title="Enter run comment"
    		data-placement="bottom" data-emptyText="Click to add a comment to your run">${run.comment!}</a>
    </div>      

    <div style="clear:both">
            <#if run.running>
                <!-- Refreshing output -->
                <script type="text/javascript">
                    var running = true;
                    function refreshOuput() {
                      $.ajax({
                        type: "GET",
                        url: "${rc.contextPath}/project/${run.project.id}/run/${run.id}/output",
                        success: function(data) {
                            console.log(data);
                            if(data.running){
                                $("#runOuput").html(data.jobOutput);
                                $("#runResultCSV").html(data.csvresult);
                                $("#numberOfSamples").html(data.numberOfSamples);
                                $("#lastSampleDate").html(data.lastSampleDate);
                            } else{
                                running = false;
                                $("#isRunning").html("false");
                            }
                        },
                        complete: function() {
                            if(running) setTimeout(refreshOuput,3000); //now that the request is complete, do it again in 5 seconds
                        }
                      });
                    }
                    jQuery(document).ready(function(){
                        refreshOuput();
                    });
                </script>
            </#if>
            <#if run.launched>
            	<@graph_panel.main />
            </#if>
        </ul>
    </div>
    <@script_variable.main run.scriptVersion run.launched false run.customScriptVariables/>
</@layout.main>
