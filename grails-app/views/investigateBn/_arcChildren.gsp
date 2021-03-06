<g:if test="${children?.size() > 0}">
	<label>
		Child node:
		<g:if test="${children?.size() == 1}">

			${children[ 0 ].readableLabel} (only one child)

			<script>
				$( '#arcSummary' ).load(
					'${createLink(action: 'ajaxArcSummary')}',
					{
						parentId : ${parent.id},
						childId  : ${children[ 0 ].id}
					}
				)
			</script>

		</g:if>
		<g:else test="${children?.size() > 1}">

			<bnInvestigate:childVariableList id="childList" name="childList" variables="${children}" selectedId="${child ? child.id : null}" assignedToUser="${user}" />

			<script>
				$( '#childList' ).change(function() {
					var url = '${createLink( action : 'arcs', params : [ userId : user?.id ?: 0 ] )}';
					document.location = url + '&parentId=${parent.id}&childId=' + $( this ).val();
				});
			</script>

		</g:else>
</g:if>
<g:else>
	No children of variable ${parent.readableLabel}
</g:else>
</label>